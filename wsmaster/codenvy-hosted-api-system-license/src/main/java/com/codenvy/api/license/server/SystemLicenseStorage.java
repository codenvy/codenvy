/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
