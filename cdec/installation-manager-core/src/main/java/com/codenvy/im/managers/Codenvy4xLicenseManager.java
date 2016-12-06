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
package com.codenvy.im.managers;

import com.codenvy.api.license.SystemLicense;
import com.codenvy.api.license.SystemLicenseFactory;
import com.codenvy.api.license.exception.InvalidSystemLicenseException;
import com.codenvy.api.license.exception.SystemLicenseException;
import com.codenvy.api.license.exception.SystemLicenseNotFoundException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;

import java.io.IOException;

import static com.google.api.client.repackaged.com.google.common.base.Strings.isNullOrEmpty;

/**
 * Codenvy license manager for 4xx.
 *
 * @author Anatoliy Bazko
 * @author Alexander Andrienko
 */
@Singleton
public class Codenvy4xLicenseManager {
    protected static final String CODENVY_LICENSE_KEY = "codenvy-license-key";

    private final StorageManager       storageManager;
    private final SystemLicenseFactory licenseFactory;

    @Inject
    public Codenvy4xLicenseManager(StorageManager storageManager, SystemLicenseFactory licenseFactory) {
        this.storageManager = storageManager;
        this.licenseFactory = licenseFactory;
    }

    /**
     * Loads Codenvy license out of underlying storage.
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
        String licenseText;
        try {
            licenseText = storageManager.loadProperty(CODENVY_LICENSE_KEY);
        } catch (StorageNotFoundException | PropertyNotFoundException e) {
            throw new SystemLicenseNotFoundException("Codenvy license not found");
        } catch (IOException e) {
            throw new SystemLicenseException(e.getMessage(), e);
        }

        if (isNullOrEmpty(licenseText)) {
            throw new SystemLicenseNotFoundException("Codenvy license not found");
        }

        return licenseFactory.create(licenseText);
    }
}
