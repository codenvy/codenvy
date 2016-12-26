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
import com.codenvy.api.license.exception.InvalidSystemLicenseException;
import com.codenvy.api.license.exception.SystemLicenseNotActivatedException;
import com.codenvy.api.license.exception.SystemLicenseNotFoundException;
import com.codenvy.api.license.shared.model.Constants;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.ValidationStatus;

import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Named;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class SystemLicenseActivator {

    private final SystemLicenseStorage systemLicenseStorage;
    private final String               publicKey;
    private final char[]               productId;

    @Inject
    public SystemLicenseActivator(@Named("license-manager.public_key") String publicKey,
                                  SystemLicenseStorage systemLicenseStorage) {
        this.systemLicenseStorage = systemLicenseStorage;
        this.publicKey = publicKey;
        this.productId = Constants.PRODUCT_ID;
    }

    /**
     * For testing purpose.
     */
    @Deprecated
    SystemLicenseActivator(SystemLicenseStorage systemLicenseStorage, String publicKey, char[] productId) {
        this.systemLicenseStorage = systemLicenseStorage;
        this.publicKey = publicKey;
        this.productId = productId;
    }

    /**
     * Activates license if it requires.
     *
     * @return activation text or {@code null} if license doesn't require activation
     */
    @Nullable
    public String activateIfRequired(SystemLicense systemLicense) {
        try {
            validateActivation(systemLicense);
            return null;
        } catch (SystemLicenseNotActivatedException e) {
            return doActivate(systemLicense);
        }
    }

    private String doActivate(SystemLicense systemLicense) {
        License license = LicenseValidator.autoActivate(systemLicense.getOrigin());
        if (license.getValidationStatus() != ValidationStatus.LICENSE_VALID) {
            throw new InvalidSystemLicenseException("Codenvy activation license text is not valid");
        }

        switch (license.getActivationStatus()) {
            case ACTIVATION_COMPLETED:
                return license.getLicenseString();
            default:
                throw new SystemLicenseNotActivatedException("System license activation failed. Error code: " + license.getActivationStatus());
        }
    }

    /**
     * Checks if system license requires activation.
     *
     * @param systemLicense
     *      system license to check
     * @throws SystemLicenseNotActivatedException
     *      if activation is required
     */
    public void validateActivation(SystemLicense systemLicense) {
        if (!systemLicense.isActivationRequired()) {
            return;
        }

        try {
            String activatedLicenseText = systemLicenseStorage.loadActivatedLicense();
            License license = LicenseValidator.validate(activatedLicenseText, publicKey, String.valueOf(productId), null, null, null, null);
            switch (license.getValidationStatus()) {
                case LICENSE_INVALID:
                    throw new SystemLicenseNotActivatedException(
                        "Codenvy activation license text is not valid. Error code: " + license.getValidationStatus());

                default:
                    break;
            }

            switch (license.getActivationStatus()) {
                case ACTIVATION_COMPLETED:
                    return;
                default:
                    throw new SystemLicenseNotActivatedException(
                            "System license is not activated. Error code: " + license.getActivationStatus());
            }
        } catch (SystemLicenseNotFoundException e) {
            throw new SystemLicenseNotActivatedException("System license is not activated.");
        }
    }
}
