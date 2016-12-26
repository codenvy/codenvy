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

import com.codenvy.api.license.exception.SystemLicenseException;
import com.codenvy.api.license.exception.SystemLicenseNotFoundException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Persists / loads / deletes system license in the storage.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class SystemLicenseStorage {
    private final Path licenseFile;
    private final Path activatedLicenseFile;

    @Inject
    public SystemLicenseStorage(@Named("license-manager.license-file") String licenseFile) {
        this.licenseFile = Paths.get(licenseFile);
        this.activatedLicenseFile = Paths.get(licenseFile + ".activated");
    }

    /**
     * Persists system license text.
     * Initialize storage if needed.
     *
     * @param licenseText
     *      the system license text to persist
     * @throws NullPointerException
     *      if licenseText is null
     * @throws SystemLicenseException
     *      if unexpected error occurred
     */
    public void persistLicense(String licenseText) throws SystemLicenseException {
        Objects.requireNonNull(licenseText, "System license text can't be null");
        doPersist(licenseText, licenseFile);
    }

    /**
     * Persists Codenvy activated license text.
     * Initialize storage if needed.
     *
     * @param activatedLicenseText
     *      the system license text to persist
     * @throws NullPointerException
     *      if licenseText is null
     * @throws SystemLicenseException
     *      if unexpected error occurred
     */
    public void persistActivatedLicense(String activatedLicenseText) {
        Objects.requireNonNull(activatedLicenseText, "System license text can't be null");
        doPersist(activatedLicenseText, activatedLicenseFile);
    }

    /**
     * Cleans the storage.
     *
     * @throws SystemLicenseException
     *      if unexpected error occurred
     */
    public void clean() throws SystemLicenseException {
        try {
            Files.deleteIfExists(licenseFile);
            Files.deleteIfExists(activatedLicenseFile);
        } catch (IOException e) {
            throw new SystemLicenseException("Unexpected error. System license can't be removed.", e);
        }
    }

    /**
     * Returns system license text.
     *
     * @throws SystemLicenseException
     *      if unexpected error occurred
     * @throws SystemLicenseNotFoundException
     *      if license file not found
     */
    public String loadLicense() {
        return doLoadLicense(licenseFile);
    }

    /**
     * Returns Codenvy activated license text.
     *
     * @throws SystemLicenseException
     *      if unexpected error occurred
     * @throws SystemLicenseNotFoundException
     *      if license file not found
     */
    public String loadActivatedLicense() {
        return doLoadLicense(activatedLicenseFile);
    }

    private String doLoadLicense(Path licenseFile) throws SystemLicenseException {
        try {
            return new String(Files.readAllBytes(licenseFile), UTF_8);
        } catch (NoSuchFileException e) {
            throw new SystemLicenseNotFoundException("System license not found");
        } catch (IOException e) {
            throw new SystemLicenseException(e.getMessage(), e);
        }
    }

    private void doPersist(String licenseText, Path licenseFile) {
        try {
            if (Files.notExists(licenseFile.getParent())) {
                initStorage();
            }

            Files.write(licenseFile, licenseText.getBytes(UTF_8));
        } catch (IOException e) {
            throw new SystemLicenseException("Unexpected error. System license can't be persisted.", e);
        }
    }

    private void initStorage() throws IOException {
        Files.createDirectories(licenseFile.getParent());
    }
}
