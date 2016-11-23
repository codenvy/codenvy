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
package com.codenvy.api.license;

import com.codenvy.api.license.exception.IllegalLicenseFormatException;
import com.codenvy.api.license.exception.InvalidLicenseException;
import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.ValidationStatus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class CodenvyLicenseFactory {

    private final char[] productId;
    private final String publicKey;

    @Inject
    public CodenvyLicenseFactory(@Named("license-manager.public_key") String publicKey) {
        this.productId = "OPL-STN-SM".toCharArray();
        this.publicKey = publicKey;
    }

    /** For testing purpose only */
    @Deprecated
    CodenvyLicenseFactory(char[] productId, String publicKey) {
        this.productId = productId;
        this.publicKey = publicKey;
    }

    /**
     * Creates valid Codenvy license.
     *
     * @param licenseText
     *         the license text
     * @throws InvalidLicenseException
     *         if license is invalid
     */
    public CodenvyLicense create(String licenseText) throws InvalidLicenseException {
        License license = LicenseValidator.validate(licenseText, publicKey, String.valueOf(productId), null, null, null, null);
        ValidationStatus licenseValidationStatus = license.getValidationStatus();
        if (licenseValidationStatus != ValidationStatus.LICENSE_VALID) {
            throw new InvalidLicenseException("Codenvy license is not valid");
        }

        HashMap<String, String> customSignedFeatures = license.getLicenseText().getCustomSignedFeatures();

        try {
            Map<LicenseFeature, String> features = customSignedFeatures
                    .entrySet()
                    .stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(LicenseFeature.valueOf(entry.getKey().toUpperCase()), entry.getValue()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            validateFeaturesFormat(features);

            return new CodenvyLicense(licenseText, features);
        } catch (IllegalArgumentException e) {
            throw new InvalidLicenseException(e.getMessage(), e);
        }
    }

    private void validateFeaturesFormat(Map<LicenseFeature, String> features) throws IllegalLicenseFormatException {
        features.entrySet().stream().forEach(entry -> entry.getKey().validateFormat(entry.getValue()));
    }
}
