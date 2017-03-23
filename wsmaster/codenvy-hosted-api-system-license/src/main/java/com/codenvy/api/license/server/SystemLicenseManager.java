/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.api.license.server;

import com.codenvy.api.license.SystemLicense;
import com.codenvy.api.license.SystemLicenseFactory;
import com.codenvy.api.license.exception.InvalidSystemLicenseException;
import com.codenvy.api.license.exception.SystemLicenseException;
import com.codenvy.api.license.exception.SystemLicenseNotFoundException;
import com.codenvy.api.license.server.dao.SystemLicenseActionDao;
import com.codenvy.api.license.server.model.impl.SystemLicenseActionImpl;
import com.codenvy.api.license.shared.dto.IssueDto;
import com.codenvy.api.license.shared.model.Constants;
import com.codenvy.api.license.shared.model.Issue;
import com.codenvy.api.permission.server.SystemDomain;
import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.event.UserCreatedEvent;
import org.eclipse.che.api.user.server.event.UserRemovedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.core.db.DBInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.FAIR_SOURCE_LICENSE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 * @author Alexander Andrienko
 */
@Singleton
public class SystemLicenseManager implements SystemLicenseManagerObservable {
    private static final Logger LOG                            = LoggerFactory.getLogger(SystemLicenseManager.class);
    private static final int    INVALIDATED_TOTAL_USERS_NUMBER = -1;

    private final SystemLicenseFactory               licenseFactory;
    private final SystemLicenseActionDao             systemLicenseActionDao;
    private final SystemLicenseStorage               systemLicenseStorage;
    private final SystemLicenseActivator             systemLicenseActivator;
    private final List<SystemLicenseManagerObserver> observers;
    private final AtomicLong                         totalNumberRef;
    private final UserManager                        userManager;

    @Inject
    @SuppressWarnings("unused")
    private DBInitializer dbInitializer;

    @Inject
    public SystemLicenseManager(SystemLicenseFactory licenseFactory,
                                UserManager userManager,
                                SystemLicenseActionDao systemLicenseActionDao,
                                SystemLicenseStorage systemLicenseStorage,
                                SystemLicenseActivator systemLicenseActivator,
                                EventService eventService) {
        this.licenseFactory = licenseFactory;
        this.userManager = userManager;
        this.systemLicenseActionDao = systemLicenseActionDao;
        this.systemLicenseStorage = systemLicenseStorage;
        this.systemLicenseActivator = systemLicenseActivator;
        this.observers = new LinkedList<>();
        this.totalNumberRef = new AtomicLong(INVALIDATED_TOTAL_USERS_NUMBER);

        eventService.subscribe(e -> invalidateTotalUsersNumber(), UserCreatedEvent.class);
        eventService.subscribe(e -> invalidateTotalUsersNumber(), UserRemovedEvent.class);
    }

    /**
     * Stores valid system license into the storage.
     *
     * @throws NullPointerException
     *         if {@code licenseText} is null
     * @throws SystemLicenseException
     *         if error occurred while storing
     */
    public void store(@NotNull String licenseText) throws SystemLicenseException, ApiException {
        requireNonNull(licenseText, "Codenvy license can't be null");

        SystemLicense systemLicense = licenseFactory.create(licenseText);
        String activatedLicenseText = systemLicenseActivator.activateIfRequired(systemLicense);
        if (activatedLicenseText != null) {
            systemLicenseStorage.persistActivatedLicense(activatedLicenseText);
        }
        systemLicenseStorage.persistLicense(systemLicense.getLicenseText());

        for (SystemLicenseManagerObserver observer : observers) {
            observer.onProductLicenseStored(systemLicense);
        }
    }

    /**
     * Loads system license out of underlying storage.
     *
     * @throws SystemLicenseNotFoundException
     *         if license not found
     * @throws InvalidSystemLicenseException
     *         if license not valid
     * @throws SystemLicenseException
     *         if error occurred while loading license
     */
    @Nullable
    public SystemLicense load() throws SystemLicenseException {
        String licenseText = systemLicenseStorage.loadLicense();
        SystemLicense systemLicense = licenseFactory.create(licenseText);
        systemLicenseActivator.validateActivation(systemLicense);
        return systemLicense;
    }

    /**
     * Removes system license from the storage.
     *
     * @throws SystemLicenseNotFoundException
     *      if system license not found
     * @throws SystemLicenseException
     *       if error occurred while deleting license
     */
    public void remove() throws SystemLicenseException, ApiException {
        String licenseText = systemLicenseStorage.loadLicense();

        try {
            SystemLicense license = licenseFactory.create(licenseText);
            systemLicenseStorage.clean();

            for (SystemLicenseManagerObserver observer : observers) {
                observer.onProductLicenseRemoved(license);
            }
        } catch (InvalidSystemLicenseException e) {
            systemLicenseStorage.clean();
        }
    }

    /**
     * Return true if only Codenvy usage meets the constrains of license properties or free usage properties.
     **/
    public boolean isSystemUsageLegal() throws ServerException, IOException {
        long actualUsers = getTotalUsersNumber();

        try {
            SystemLicense systemLicense = load();
            return systemLicense.isLicenseUsageLegal(actualUsers);
        } catch (SystemLicenseException e) {
            return SystemLicense.isFreeUsageLegal(actualUsers);
        }
    }

    /**
     * Check whether current license allows adding new users due to its capacity.
     * @throws ServerException
     */
    public boolean canUserBeAdded() throws ServerException {
        return isLicenseUsageLegal(getTotalUsersNumber() + 1);
    }

    /**
     * Returns allowed number of users due to actual license.
     */
    public long getAllowedUserNumber() {
        try {
            SystemLicense systemLicense = load();
            if (!systemLicense.isTimeForRenewExpired()) {
                return systemLicense.getNumberOfUsers();
            } else {
                return SystemLicense.MAX_NUMBER_OF_FREE_USERS;
            }
        } catch (SystemLicenseException e) {
            return SystemLicense.MAX_NUMBER_OF_FREE_USERS;
        }
    }

    /**
     * Returns list of issues related to actual license.
     */
    public List<IssueDto> getLicenseIssues() throws ApiException, IOException {
        List<IssueDto> issues = new ArrayList<>();

        if (!canUserBeAdded()) {
            issues.add(newDto(IssueDto.class).withStatus(Issue.Status.USER_LICENSE_HAS_REACHED_ITS_LIMIT)
                                             .withMessage(Constants.LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_REGISTRATION));
        }

        if (!isFairSourceLicenseAccepted()) {
            issues.add(newDto(IssueDto.class).withStatus(Issue.Status.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED)
                                             .withMessage(Constants.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE));
        }

        try {
            if (isPaidLicenseExpiring()) {
                issues.add(newDto(IssueDto.class).withStatus(Issue.Status.LICENSE_EXPIRING)
                                                 .withMessage(getMessageForLicenseExpiring()));
            } else if (isTimeForPaidLicenseRenewExpired() && !isSystemUsageLegal()) {
                issues.add(newDto(IssueDto.class).withStatus(Issue.Status.LICENSE_EXPIRED)
                                                 .withMessage(getMessageForLicenseCompletelyExpired()));
            }
        } catch (SystemLicenseException e) {
            // do nothing if there is no valid paid system license.
        }

        return issues;
    }

    /**
     * Accepts Codenvy Fair Source License
     *
     * @see SystemLicenseActionDao#insert(SystemLicenseActionImpl)
     *
     * @throws ConflictException
     *      if license already has been accepted
     */
    public void acceptFairSourceLicense() throws ApiException {
        for (SystemLicenseManagerObserver observer : observers) {
            observer.onCodenvyFairSourceLicenseAccepted();
        }
    }

    /**
     * Indicates if Codenvy Fair Source License is accepted.
     */
    public boolean isFairSourceLicenseAccepted() throws ServerException {
        try {
            systemLicenseActionDao.getByLicenseTypeAndAction(FAIR_SOURCE_LICENSE, ACCEPTED);
        } catch (NotFoundException e) {
            return false;
        }

        return true;
    }

    @Override
    public void addObserver(SystemLicenseManagerObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(SystemLicenseManagerObserver observer) {
        observers.remove(observer);
    }

    /**
     * Returns error message for the case when license expired but there is additional time for renew it.
     */
    public String getMessageForLicenseExpiring() {
        return format(Constants.LICENSE_EXPIRING_MESSAGE_TEMPLATE,
                      SystemLicense.MAX_NUMBER_OF_FREE_USERS,
                      load().daysBeforeTimeForRenewExpires());
    }

    /**
     * Returns error message when license completely expired (including additional time for renew license) with different content
     * depending on if current user is admin.
     * @throws ServerException
     */
    public String getMessageForLicenseCompletelyExpired() throws ServerException {
        if (isAdmin()) {
            return format(Constants.LICENSE_COMPLETELY_EXPIRED_MESSAGE_FOR_ADMIN_TEMPLATE,
                          getTotalUsersNumber(),
                          SystemLicense.MAX_NUMBER_OF_FREE_USERS);
        } else {
            return Constants.LICENSE_COMPLETELY_EXPIRED_MESSAGE_FOR_NON_ADMIN;
        }
    }

    /**
     * Returns error message when license completely expired (including additional time for renew license) with different content
     * depending on if current user is admin, and if there is existed license in the system.
     * @throws ServerException
     */
    public String getMessageWhenUserCannotStartWorkspace() throws ServerException {
        try {
            // when license exists
            SystemLicense license = load();   // check if license exists
            if (license.isTimeForRenewExpired()) {
                return getMessageForLicenseCompletelyExpired();
            }
        } catch (SystemLicenseException e) {
            // do nothing
        }

        // when license absent, invalid or non-completely-expired
        if (isAdmin()) {
            return format(Constants.LICENSE_COMPLETELY_EXPIRED_MESSAGE_FOR_ADMIN_TEMPLATE,
                          getTotalUsersNumber(),
                          SystemLicense.MAX_NUMBER_OF_FREE_USERS);
        } else {
            return Constants.LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_WORKSPACE;
        }
    }

    /**
     * Returns true if only actual license conditions allow to start workspace.
     * @throws ServerException
     */
    public boolean canStartWorkspace() throws ServerException {
        return isLicenseUsageLegal(getTotalUsersNumber());
    }

    @VisibleForTesting
    boolean isLicenseUsageLegal(long userNumber) throws ServerException {
        try {
            SystemLicense systemLicense = load();
            return systemLicense.isLicenseUsageLegal(userNumber);
        } catch (SystemLicenseException e) {
            return SystemLicense.isFreeUsageLegal(userNumber);
        }
    }

    @VisibleForTesting
    boolean isPaidLicenseExpiring() {
        return load().isExpiring();
    }

    @VisibleForTesting
    boolean isTimeForPaidLicenseRenewExpired() throws ApiException {
        SystemLicense license = load();
        if (license.isTimeForRenewExpired()) {
            for (SystemLicenseManagerObserver observer : observers) {
                observer.onProductLicenseExpired(license);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isAdmin() {
        return EnvironmentContext.getCurrent().getSubject().hasPermission(SystemDomain.DOMAIN_ID,
                                                                          null,
                                                                          SystemDomain.MANAGE_SYSTEM_ACTION);
    }

    private void invalidateTotalUsersNumber() {
        totalNumberRef.set(INVALIDATED_TOTAL_USERS_NUMBER);
    }

    private long getTotalUsersNumber() throws ServerException {
        long totalNumber = totalNumberRef.updateAndGet(currentUsersNumber -> {
            if (currentUsersNumber == INVALIDATED_TOTAL_USERS_NUMBER) {
                try {
                    return userManager.getTotalCount();
                } catch (ServerException e) {
                    LOG.error("Can't get total users number. License checking might be inconsistent.", e);
                    return INVALIDATED_TOTAL_USERS_NUMBER;
                }
            } else {
                return currentUsersNumber;
            }
        });

        if (totalNumber == INVALIDATED_TOTAL_USERS_NUMBER) {
            throw new ServerException("It is impossible to perform system license checking because the total number of users is unknown.");
        }

        return totalNumber;
    }
}
