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
import com.codenvy.api.license.shared.model.FairSourceLicenseAcceptance;
import com.codenvy.api.license.shared.model.Issue;
import com.codenvy.swarm.client.SwarmDockerConnector;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.License.FAIR_SOURCE_LICENSE;
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

    public static final String UNABLE_TO_ADD_ACCOUNT_BECAUSE_OF_LICENSE   = "Unable to add your account. The Codenvy license has reached its user limit.";
    public static final String LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE = "Your user license has reached its limit. You cannot add more users.";

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
        long actualUsers = userManager.getTotalCount();

        try {
            SystemLicense systemLicense = load();
            return systemLicense.isLicenseUsageLegal(actualUsers + 1, 0);
        } catch (SystemLicenseException e) {
            return SystemLicense.isFreeUsageLegal(actualUsers + 1, 0);
        }
    }

    /**
     * Returns allowed number of users due to actual license.
     */
    public long getAllowedUserNumber() {
        try {
            SystemLicense systemLicense = load();
            return systemLicense.getNumberOfUsers();
        } catch (SystemLicenseException e) {
            return SystemLicense.MAX_NUMBER_OF_FREE_USERS;
        }
    }

    /**
     * Returns list of issues related to actual license.
     */
    public List<IssueDto> getLicenseIssues() throws ServerException {
        List<IssueDto> issues = new ArrayList<>();

        if (!canUserBeAdded()) {
            final IssueDto userLicenseHasReachedItsLimitIssue = newDto(IssueDto.class).withStatus(Issue.Status.USER_LICENSE_HAS_REACHED_ITS_LIMIT)
                                                                                      .withMessage(LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE);
            issues.add(userLicenseHasReachedItsLimitIssue);
        }

        return issues;
    }

    /**
     * Accepts Codenvy Fair Source License
     *
     * @see SystemLicenseActionDao#insert(SystemLicenseActionImpl)
     *
     * @param fairSourceLicenseAcceptance
     *      acceptance request
     * @throws ConflictException
     *      if license already has been accepted
     * @throws BadRequestException
     *      if request is not complete
     */
    public void acceptFairSourceLicense(FairSourceLicenseAcceptance fairSourceLicenseAcceptance) throws ApiException {
        for (SystemLicenseManagerObserver observer : observers) {
            observer.onCodenvyFairSourceLicenseAccepted(fairSourceLicenseAcceptance);
        }
    }

    /**
     * Indicates if Codenvy Fair Source License is accepted.
     */
    public boolean hasAcceptedFairSourceLicense() throws ServerException {
        try {
            systemLicenseActionDao.getByLicenseAndAction(FAIR_SOURCE_LICENSE, ACCEPTED);
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
}
