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

import com.codenvy.api.license.shared.dto.IssueDto;
import com.codenvy.api.license.shared.model.Issue;
import com.codenvy.api.license.shared.CodenvyLicense;
import com.codenvy.api.license.shared.CodenvyLicenseFactory;
import com.codenvy.api.license.shared.exception.InvalidLicenseException;
import com.codenvy.api.license.shared.exception.LicenseException;
import com.codenvy.api.license.shared.exception.LicenseNotFoundException;
import com.codenvy.swarm.client.SwarmDockerConnector;
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
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 * @author Alexander Andrienko
 */
@Singleton
public class CodenvyLicenseManager {

    private final CodenvyLicenseFactory licenseFactory;
    private final Path                  licenseFile;
    private final UserManager           userManager;
    private final SwarmDockerConnector  dockerConnector;

    public static final String LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE =
        "Unable to add your account. The Codenvy license has reached its user limit.";

    @Inject
    public CodenvyLicenseManager(@Named("license-manager.license-file") String licenseFile,
                                 CodenvyLicenseFactory licenseFactory,
                                 UserManager userManager,
                                 SwarmDockerConnector dockerConnector) {
        this.licenseFactory = licenseFactory;
        this.licenseFile = Paths.get(licenseFile);
        this.userManager = userManager;
        this.dockerConnector = dockerConnector;
    }

    /**
     * Stores valid Codenvy license into the storage.
     *
     * @throws NullPointerException
     *         if {@code licenseText} is null
     * @throws LicenseException
     *         if error occurred while storing
     */
    public void store(@NotNull String licenseText) throws LicenseException {
        Objects.requireNonNull(licenseText, "Codenvy license must not be null");
        CodenvyLicense codenvyLicense = licenseFactory.create(licenseText);

        try {
            Files.write(licenseFile, codenvyLicense.getLicenseText().getBytes());
        } catch (IOException e) {
            throw new LicenseException(e.getMessage(), e);
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
        String licenseText;
        try {
            licenseText = new String(Files.readAllBytes(licenseFile), UTF_8);
        } catch (NoSuchFileException e) {
            throw new LicenseNotFoundException("Codenvy license not found");
        } catch (IOException e) {
            throw new LicenseException(e.getMessage(), e);
        }

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
    public void delete() throws LicenseException {
        try {
            Files.delete(licenseFile);
        } catch (NoSuchFileException e) {
            throw new LicenseNotFoundException("Codenvy license not found");
        } catch (IOException e) {
            throw new LicenseException(e.getMessage(), e);
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
}
