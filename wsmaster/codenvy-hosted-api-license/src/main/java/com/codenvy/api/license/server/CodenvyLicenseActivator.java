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
import com.codenvy.api.license.exception.InvalidLicenseException;
import com.codenvy.api.license.exception.LicenseNotActivatedException;
import com.codenvy.api.license.exception.LicenseNotFoundException;
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
public class CodenvyLicenseActivator {

    private final CodenvyLicenseStorage codenvyLicenseStorage;
    private final String                publicKey;
    private final char[]                productId;

    @Inject
    public CodenvyLicenseActivator(@Named("license-manager.public_key") String publicKey,
                                   CodenvyLicenseStorage codenvyLicenseStorage) {
        this.codenvyLicenseStorage = codenvyLicenseStorage;
        this.publicKey = publicKey;
        this.productId = Constants.PRODUCT_ID;
    }

    /**
     * Activates license if it requires.
     *
     * @return Codenvy license activation text or {@code null} if license doesn't require activation
     */
    @Nullable
    public String activateIfRequired(CodenvyLicense codenvyLicense) {
        try {
            validateActivation(codenvyLicense);
            return null;
        } catch (LicenseNotActivatedException e) {
            return doActivate(codenvyLicense);
        }
    }

    private String doActivate(CodenvyLicense codenvyLicense) {
        License license = LicenseValidator.autoActivate(codenvyLicense.getOrigin());
        if (license.getValidationStatus() != ValidationStatus.LICENSE_VALID) {
            throw new InvalidLicenseException("Codenvy activation license text is not valid");
        }

        switch (license.getActivationStatus()) {
            case ACTIVATION_COMPLETED:
                return license.getLicenseString();
            default:
                throw new LicenseNotActivatedException("Codenvy license activation failed. Error code: " + license.getActivationStatus());
        }
    }

    /**
     * Checks if Codenvy license requires activation.
     *
     * @param codenvyLicense
     *      Codenvy license to check
     * @throws LicenseNotActivatedException
     *      if activation is required
     */
    public void validateActivation(CodenvyLicense codenvyLicense) {
        if (!codenvyLicense.isActivationRequired()) {
            return;
        }

        try {
            String activatedLicenseText = codenvyLicenseStorage.loadActivatedLicense();
            License license = LicenseValidator.validate(activatedLicenseText, publicKey, String.valueOf(productId), null, null, null, null);
            switch (license.getValidationStatus()) {
                case LICENSE_VALID:
                case MISMATCH_HARDWARE_ID:
                    break;
                default:
                    throw new LicenseNotActivatedException(
                            "Codenvy activation license text is not valid. Error code: " + license.getValidationStatus());
            }

            switch (license.getActivationStatus()) {
                case ACTIVATION_COMPLETED:
                    return;
                default:
                    throw new LicenseNotActivatedException(
                            "Codenvy license is not activated. Error code: " + license.getActivationStatus());
            }
        } catch (LicenseNotFoundException e) {
            throw new LicenseNotActivatedException("Codenvy license is not activated.");
        }
    }
}
