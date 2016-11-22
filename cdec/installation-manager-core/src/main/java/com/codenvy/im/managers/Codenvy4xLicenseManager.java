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

import com.codenvy.api.license.shared.CodenvyLicense;
import com.codenvy.api.license.shared.CodenvyLicenseFactory;
import com.codenvy.api.license.shared.exception.InvalidLicenseException;
import com.codenvy.api.license.shared.exception.LicenseException;
import com.codenvy.api.license.shared.exception.LicenseNotFoundException;
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

    private final StorageManager        storageManager;
    private final CodenvyLicenseFactory licenseFactory;

    @Inject
    public Codenvy4xLicenseManager(StorageManager storageManager, CodenvyLicenseFactory licenseFactory) {
        this.storageManager = storageManager;
        this.licenseFactory = licenseFactory;
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
            licenseText = storageManager.loadProperty(CODENVY_LICENSE_KEY);
        } catch (StorageNotFoundException | PropertyNotFoundException e) {
            throw new LicenseNotFoundException("Codenvy license not found");
        } catch (IOException e) {
            throw new LicenseException(e.getMessage(), e);
        }

        if (isNullOrEmpty(licenseText)) {
            throw new LicenseNotFoundException("Codenvy license not found");
        }

        return licenseFactory.create(licenseText);
    }
}
