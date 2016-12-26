/*
 *  [2012] - [2016] Codenvy, S.A.
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
import com.codenvy.api.license.shared.model.Issue;
import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.google.common.annotations.VisibleForTesting;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.PRODUCT_LICENSE;
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

    private final SystemLicenseFactory               licenseFactory;
    private final UserManager                        userManager;
    private final SwarmDockerConnector               dockerConnector;
    private final SystemLicenseActionDao             systemLicenseActionDao;
    private final SystemLicenseStorage               systemLicenseStorage;
    private final SystemLicenseActivator             systemLicenseActivator;
    private final List<SystemLicenseManagerObserver> observers;

    public static final String LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_REGISTRATION = "Your user license has reached its limit. You cannot add more users.";
    public static final String LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_WORKSPACE    = "The Codenvy license has reached its user limit - "
                                                                                             + "you can access the user dashboard but not the IDE.";
    public static final String UNABLE_TO_ADD_ACCOUNT_BECAUSE_OF_LICENSE    = "Unable to add your account. The Codenvy license has reached its user limit.";

    public static final String FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE = "Your admin has not accepted the license agreement.";

    public static final String LICENSE_EXPIRING_MESSAGE_TEMPLATE           = "License expired. Codenvy will downgrade to a %s user Fair Source license in %s days.";

    public static final String LICENSE_COMPLETELY_EXPIRED_MESSAGE_FOR_ADMIN_TEMPLATE = "There are currently %s users registered in Codenvy but your license only allows %s. "
                                                                                       + "Users cannot start workspaces.";
    public static final String LICENSE_COMPLETELY_EXPIRED_MESSAGE_FOR_NON_ADMIN      = "The Codenvy license is expired - you can access the user dashboard but not the IDE.";

    @Inject
    public SystemLicenseManager(SystemLicenseFactory licenseFactory,
                                UserManager userManager,
                                SwarmDockerConnector dockerConnector,
                                SystemLicenseActionDao systemLicenseActionDao,
                                SystemLicenseStorage systemLicenseStorage,
                                SystemLicenseActivator systemLicenseActivator) {
        this.licenseFactory = licenseFactory;
        this.userManager = userManager;
        this.dockerConnector = dockerConnector;
        this.systemLicenseActionDao = systemLicenseActionDao;
        this.systemLicenseStorage = systemLicenseStorage;
        this.systemLicenseActivator = systemLicenseActivator;
        this.observers = new LinkedList<>();
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
     * Deletes system license from the storage.
     *
     * @throws SystemLicenseNotFoundException
     *      if system license not found
     * @throws SystemLicenseException
     *       if error occurred while deleting license
     */
    public void delete() throws SystemLicenseException, ApiException {
        String licenseText = systemLicenseStorage.loadLicense();

        try {
            SystemLicense systemLicense = licenseFactory.create(licenseText);
            systemLicenseStorage.clean();

            for (SystemLicenseManagerObserver observer : observers) {
                observer.onProductLicenseDeleted(systemLicense);
            }
        } catch (InvalidSystemLicenseException e) {
            systemLicenseStorage.clean();
        }
    }

    /**
     * Return true if only Codenvy usage meets the constrains of license properties or free usage properties.
     **/
    public boolean isSystemUsageLegal() throws ServerException, IOException {
        long actualUsers = userManager.getTotalCount();
        int actualServers = dockerConnector.getAvailableNodes().size();

        try {
            SystemLicense systemLicense = load();
            return systemLicense.isLicenseUsageLegal(actualUsers, actualServers);
        } catch (SystemLicenseException e) {
            return SystemLicense.isFreeUsageLegal(actualUsers, actualServers);
        }
    }

    /**
     * Return true if only node number meets the constrains of license properties or free usage properties.
     * If nodeNumber == null, uses actual number of machine nodes.
     *
     * @param nodeNumber
     *         number of machine nodes.
     */
    public boolean isSystemNodesUsageLegal(Integer nodeNumber) throws IOException {
        if (nodeNumber == null) {
            nodeNumber = dockerConnector.getAvailableNodes().size();
        }

        try {
            SystemLicense systemLicense = load();
            return systemLicense.isLicenseNodesUsageLegal(nodeNumber);
        } catch (SystemLicenseException e) {
            return SystemLicense.isFreeUsageLegal(0, nodeNumber);  // user number doesn't matter
        }
    }

    /**
     * Check whether current license allows adding new users due to its capacity.
     * @throws ServerException
     */
    public boolean canUserBeAdded() throws ServerException {
        return isLicenseUsageLegal(userManager.getTotalCount() + 1);
    }

    /**
     * Returns allowed number of users due to actual license.
     */
    public long getAllowedUserNumber() {
        try {
            SystemLicense systemLicense = load();
            if (!systemLicense.isExpiredCompletely()) {
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
    public List<IssueDto> getLicenseIssues() throws ServerException, ConflictException, IOException {
        List<IssueDto> issues = new ArrayList<>();

        if (!canUserBeAdded()) {
            issues.add(newDto(IssueDto.class).withStatus(Issue.Status.USER_LICENSE_HAS_REACHED_ITS_LIMIT)
                                             .withMessage(LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_REGISTRATION));
        }

        if (!isFairSourceLicenseAccepted()) {
            issues.add(newDto(IssueDto.class).withStatus(Issue.Status.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED)
                                             .withMessage(FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE));
        }

        try {
            if (isPaidLicenseExpiring()) {
                issues.add(newDto(IssueDto.class).withStatus(Issue.Status.LICENSE_EXPIRING)
                                                 .withMessage(getMessageForLicenseExpiring()));
            } else if (isPaidLicenseCompletelyExpired() && ! isSystemUsageLegal()) {
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
        return format(LICENSE_EXPIRING_MESSAGE_TEMPLATE,
                      SystemLicense.MAX_NUMBER_OF_FREE_USERS,
                      load().daysBeforeCompleteExpiration());
    }

    /**
     * Returns error message when license completely expired (including additional time for renew license) with different content
     * depending on if current user is admin, and if there is existed license in the system.
     * @throws ServerException
     */
    public String getMessageForLicenseCompletelyExpired() throws ServerException {
        if (isAdmin()) {
            return format(LICENSE_COMPLETELY_EXPIRED_MESSAGE_FOR_ADMIN_TEMPLATE,
                          userManager.getTotalCount(),
                          SystemLicense.MAX_NUMBER_OF_FREE_USERS);
        } else {
            try {
                load();
                return LICENSE_COMPLETELY_EXPIRED_MESSAGE_FOR_NON_ADMIN;
            } catch (SystemLicenseException e) {
                return LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_WORKSPACE;
            }
        }
    }

    /**
     * Returns true if only actual license conditions allow to start workspace.
     * @throws ServerException
     */
    public boolean canStartWorkspace() throws ServerException {
        return isLicenseUsageLegal(userManager.getTotalCount());
    }

    @VisibleForTesting
    boolean isLicenseUsageLegal(long userNumber) throws ServerException {
        try {
            SystemLicense systemLicense = load();
            return systemLicense.isLicenseUsageLegal(userNumber, 0);
        } catch (SystemLicenseException e) {
            return SystemLicense.isFreeUsageLegal(userNumber, 0);
        }
    }

    @VisibleForTesting
    boolean isPaidLicenseExpiring() {
        return load().isExpiring();
    }

    @VisibleForTesting
    boolean isPaidLicenseCompletelyExpired() throws ServerException, ConflictException {
        SystemLicense license = load();
        if (license.isExpiredCompletely()) {
            revertToFairSourceLicense(license);
            return true;
        } else {
            return false;
        }
    }

    @VisibleForTesting
    void revertToFairSourceLicense(SystemLicense license) throws ServerException, ConflictException {
        try {
            systemLicenseActionDao.getByLicenseIdAndAction(license.getLicenseId(), EXPIRED);
        } catch (NotFoundException e) {
            SystemLicenseActionImpl codenvyLicenseAction
                = new SystemLicenseActionImpl(PRODUCT_LICENSE,
                                              EXPIRED,
                                              System.currentTimeMillis(),
                                              license.getLicenseId(),
                                              Collections.emptyMap());

            systemLicenseActionDao.upsert(codenvyLicenseAction);
        }
    }

    private boolean isAdmin() {
        Subject subject = EnvironmentContext.getCurrent().getSubject();
        if (subject != null) {
            return subject.hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
        }

        return false;
    }
}
