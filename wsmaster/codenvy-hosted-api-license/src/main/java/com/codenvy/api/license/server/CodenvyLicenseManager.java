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
import com.codenvy.api.license.shared.model.Constants;
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
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.shared.model.Constants.License.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.shared.model.Constants.License.PRODUCT_LICENSE;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 * @author Alexander Andrienko
 */
@Singleton
public class CodenvyLicenseManager {

    private final CodenvyLicenseFactory   licenseFactory;
    private final Path                    licenseFile;
    private final UserManager             userManager;
    private final SwarmDockerConnector    dockerConnector;
    private final CodenvyLicenseActionDao codenvyLicenseActionDao;

    public static final String UNABLE_TO_ADD_ACCOUNT_BECAUSE_OF_LICENSE   = "Unable to add your account. The Codenvy license has reached its user limit.";
    public static final String LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE = "Your user license has reached its limit. You cannot add more users.";

    @Inject
    public CodenvyLicenseManager(@Named("license-manager.license-file") String licenseFile,
                                 CodenvyLicenseFactory licenseFactory,
                                 UserManager userManager,
                                 SwarmDockerConnector dockerConnector,
                                 CodenvyLicenseActionDao codenvyLicenseActionDao) {
        this.licenseFactory = licenseFactory;
        this.licenseFile = Paths.get(licenseFile);
        this.userManager = userManager;
        this.dockerConnector = dockerConnector;
        this.codenvyLicenseActionDao = codenvyLicenseActionDao;
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
        try {
            Files.write(licenseFile, codenvyLicense.getLicenseText().getBytes());
        } catch (IOException e) {
            throw new LicenseException(e.getMessage(), e);
        }

        removeActionsOfExpiredLicense();
        removeActionsOfDifferentLicenseAndStoreNew(codenvyLicense.getLicenseId());
    }

    private void removeActionsOfDifferentLicenseAndStoreNew(String licenseQualifier) throws ApiException {
        try {
            CodenvyLicenseActionImpl licenseAction = codenvyLicenseActionDao.getByLicenseAndAction(PRODUCT_LICENSE, ACCEPTED);
            if (!licenseAction.getLicenseQualifier().equals(licenseQualifier)) {
                codenvyLicenseActionDao.remove(PRODUCT_LICENSE, ACCEPTED);
                codenvyLicenseActionDao.remove(PRODUCT_LICENSE, EXPIRED);
                addLicenseAction(PRODUCT_LICENSE, ACCEPTED, licenseQualifier);
            }
        } catch (NotFoundException e) {
            addLicenseAction(PRODUCT_LICENSE, ACCEPTED, licenseQualifier);
        }
    }

    private void removeActionsOfExpiredLicense() throws ServerException {
        try {
            codenvyLicenseActionDao.getByLicenseAndAction(PRODUCT_LICENSE, EXPIRED);
            codenvyLicenseActionDao.remove(PRODUCT_LICENSE, ACCEPTED);
            codenvyLicenseActionDao.remove(PRODUCT_LICENSE, EXPIRED);
        } catch (NotFoundException ignored) {
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
        String licenseText = readLicenseText();
        if (isNullOrEmpty(licenseText)) {
            throw new LicenseNotFoundException("Codenvy license not found");
        }

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

        try {
            Files.delete(licenseFile);
        } catch (NoSuchFileException e) {
            throw new LicenseNotFoundException("Codenvy license not found");
        } catch (IOException e) {
            throw new LicenseException(e.getMessage(), e);
        }

        codenvyLicenseActionDao.remove(PRODUCT_LICENSE, EXPIRED);
        addLicenseAction(PRODUCT_LICENSE, EXPIRED, codenvyLicense.getLicenseId());
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
     * @see CodenvyLicenseActionDao#store(CodenvyLicenseActionImpl)
     *
     * @param fairSourceLicenseAcceptance
     *      acceptance request
     * @throws ConflictException
     *      if license already has been accepted
     * @throws BadRequestException
     *      if request is not complete
     */
    public void acceptFairSourceLicense(FairSourceLicenseAcceptance fairSourceLicenseAcceptance) throws ApiException {
        try {
            codenvyLicenseActionDao.getByLicenseAndAction(FAIR_SOURCE_LICENSE, ACCEPTED);
            throw new ConflictException("Codenvy Fair Source License has been already accepted");
        } catch (NotFoundException e) {
            // No Codenvy Fair Source License Accepted
        }

        Map<String, String> attributes = new HashMap<>(3);
        attributes.put("firstName", fairSourceLicenseAcceptance.getFirstName());
        attributes.put("lastName", fairSourceLicenseAcceptance.getLastName());
        attributes.put("email", fairSourceLicenseAcceptance.getEmail());
        addLicenseAction(FAIR_SOURCE_LICENSE, ACCEPTED, null, attributes);
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

    private void addLicenseAction(Constants.License licenseType,
                                  Constants.Action actionType,
                                  @Nullable String licenseQualifier) throws ApiException {
        addLicenseAction(licenseType, actionType, licenseQualifier, emptyMap());
    }

    private void addLicenseAction(Constants.License licenseType,
                                  Constants.Action actionType,
                                  @Nullable String licenseQualifier,
                                  Map<String, String> attributes) throws ApiException {

        CodenvyLicenseActionImpl codenvyLicenseAction
                = new CodenvyLicenseActionImpl(licenseType,
                                               actionType,
                                               System.currentTimeMillis(),
                                               licenseQualifier,
                                               attributes);

        codenvyLicenseActionDao.store(codenvyLicenseAction);
    }

    private String readLicenseText() throws LicenseException {
        try {
            return new String(Files.readAllBytes(licenseFile), UTF_8);
        } catch (NoSuchFileException e) {
            throw new LicenseNotFoundException("Codenvy license not found");
        } catch (IOException e) {
            throw new LicenseException(e.getMessage(), e);
        }
    }
}
