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

import com.codenvy.api.license.CodenvyLicense;
import com.codenvy.api.license.CodenvyLicenseFactory;
import com.codenvy.api.license.exception.InvalidLicenseException;
import com.codenvy.api.license.exception.LicenseException;
import com.codenvy.api.license.exception.LicenseNotFoundException;
import com.codenvy.api.license.server.dao.CodenvyLicenseActionDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
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
import static com.codenvy.api.license.shared.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.shared.model.Constants.License.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.shared.model.Constants.License.PRODUCT_LICENSE;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 * @author Alexander Andrienko
 */
@Singleton
public class CodenvyLicenseManager implements CodenvyLicenseManagerObservable {

    private final CodenvyLicenseFactory               licenseFactory;
    private final UserManager                         userManager;
    private final SwarmDockerConnector                dockerConnector;
    private final CodenvyLicenseActionDao             codenvyLicenseActionDao;
    private final CodenvyLicenseStorage               codenvyLicenseStorage;
    private final List<CodenvyLicenseManagerObserver> observers;

    public static final String UNABLE_TO_ADD_ACCOUNT_BECAUSE_OF_LICENSE   = "Unable to add your account. The Codenvy license has reached its user limit.";
    public static final String LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE = "Your user license has reached its limit. You cannot add more users.";

    @Inject
    public CodenvyLicenseManager(CodenvyLicenseFactory licenseFactory,
                                 UserManager userManager,
                                 SwarmDockerConnector dockerConnector,
                                 CodenvyLicenseActionDao codenvyLicenseActionDao,
                                 CodenvyLicenseStorage codenvyLicenseStorage) {
        this.licenseFactory = licenseFactory;
        this.userManager = userManager;
        this.dockerConnector = dockerConnector;
        this.codenvyLicenseActionDao = codenvyLicenseActionDao;
        this.codenvyLicenseStorage = codenvyLicenseStorage;
        this.observers = new LinkedList<>();
    }

    /**
     * Stores valid Codenvy license into the storage.
     *
     * @throws NullPointerException
     *         if {@code licenseText} is null
     * @throws LicenseException
     *         if error occurred while storing
     */
    public void store(@NotNull String licenseText) throws LicenseException, ApiException {
        requireNonNull(licenseText, "Codenvy license can't be null");

        CodenvyLicense codenvyLicense = licenseFactory.create(licenseText);
        codenvyLicenseStorage.persistLicense(codenvyLicense.getLicenseText());

        for (CodenvyLicenseManagerObserver observer : observers) {
            observer.onProductLicenseStored(codenvyLicense);
        }
    }

    /**
     * Loads Codenvy license out of underlying storage.
     *
     * @throws LicenseNotFoundException
     *         if license not found
     * @throws InvalidLicenseException
     *         if license not valid
     * @throws LicenseException
     *         if error occurred while loading license
     */
    @Nullable
    public CodenvyLicense load() throws LicenseException {
        String licenseText = codenvyLicenseStorage.loadLicense();
        return licenseFactory.create(licenseText);
    }

    /**
     * Deletes Codenvy license from the storage.
     *
     * @throws LicenseException
     *         if error occurred while deleting license
     */
    public void delete() throws LicenseException, ApiException {
        CodenvyLicense codenvyLicense = load();
        codenvyLicenseStorage.clean();

        codenvyLicenseActionDao.remove(PRODUCT_LICENSE, EXPIRED);
        for (CodenvyLicenseManagerObserver observer : observers) {
            observer.onProductLicenseDeleted(codenvyLicense);
        }
    }

    /**
     * Return true if only Codenvy usage meets the constrains of license properties or free usage properties.
     **/
    public boolean isSystemUsageLegal() throws ServerException, IOException {
        long actualUsers = userManager.getTotalCount();
        int actualServers = dockerConnector.getAvailableNodes().size();

        try {
            CodenvyLicense codenvyLicense = load();
            return codenvyLicense.isLicenseUsageLegal(actualUsers, actualServers);
        } catch (LicenseException e) {
            return CodenvyLicense.isFreeUsageLegal(actualUsers, actualServers);
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
            CodenvyLicense codenvyLicense = load();
            return codenvyLicense.isLicenseNodesUsageLegal(nodeNumber);
        } catch (LicenseException e) {
            return CodenvyLicense.isFreeUsageLegal(0, nodeNumber);  // user number doesn't matter
        }
    }

    /**
     * Check whether current license allows adding new users due to its capacity.
     * @throws ServerException
     */
    public boolean canUserBeAdded() throws ServerException {
        long actualUsers = userManager.getTotalCount();

        try {
            CodenvyLicense codenvyLicense = load();
            return codenvyLicense.isLicenseUsageLegal(actualUsers + 1, 0);
        } catch (LicenseException e) {
            return CodenvyLicense.isFreeUsageLegal(actualUsers + 1, 0);
        }
    }

    /**
     * Returns allowed number of users due to actual license.
     */
    public long getAllowedUserNumber() {
        try {
            CodenvyLicense codenvyLicense = load();
            return codenvyLicense.getNumberOfUsers();
        } catch (LicenseException e) {
            return CodenvyLicense.MAX_NUMBER_OF_FREE_USERS;
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
     * @see CodenvyLicenseActionDao#insert(CodenvyLicenseActionImpl)
     *
     * @param fairSourceLicenseAcceptance
     *      acceptance request
     * @throws ConflictException
     *      if license already has been accepted
     * @throws BadRequestException
     *      if request is not complete
     */
    public void acceptFairSourceLicense(FairSourceLicenseAcceptance fairSourceLicenseAcceptance) throws ApiException {
        for (CodenvyLicenseManagerObserver observer : observers) {
            observer.onCodenvyFairSourceLicenseAccepted(fairSourceLicenseAcceptance);
        }
    }

    /**
     * Indicates if Codenvy Fair Source License is accepted.
     */
    public boolean hasAcceptedFairSourceLicense() throws ServerException {
        try {
            codenvyLicenseActionDao.getByLicenseAndAction(FAIR_SOURCE_LICENSE, ACCEPTED);
        } catch (NotFoundException e) {
            return false;
        }

        return true;
    }

    @Override
    public void addObserver(CodenvyLicenseManagerObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(CodenvyLicenseManagerObserver observer) {
        observers.remove(observer);
    }
}
