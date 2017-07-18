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
package com.codenvy.api.license;

import com.codenvy.api.license.exception.IllegalSystemLicenseFormatException;
import com.codenvy.api.license.exception.InvalidSystemLicenseException;
import com.codenvy.api.license.shared.model.Constants;
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
public class SystemLicenseFactory {

    private final char[] productId;
    private final String publicKey;

    @Inject
    public SystemLicenseFactory(@Named("license-manager.public_key") String publicKey) {
        this.productId = Constants.PRODUCT_ID;
        this.publicKey = publicKey;
    }

    /** For testing purpose only */
    SystemLicenseFactory(char[] productId, String publicKey) {
        this.productId = productId;
        this.publicKey = publicKey;
    }

    /**
     * Creates valid system license.
     *
     * @param licenseText
     *         the license text
     * @throws InvalidSystemLicenseException
     *         if license is invalid
     */
    public SystemLicense create(String licenseText) throws InvalidSystemLicenseException {
        License license = LicenseValidator.validate(licenseText, publicKey, String.valueOf(productId), null, null, null, null);
        ValidationStatus licenseValidationStatus = license.getValidationStatus();
        if (licenseValidationStatus.equals(ValidationStatus.LICENSE_INVALID)) {
            throw new InvalidSystemLicenseException("System license is not valid");
        }

        HashMap<String, String> customSignedFeatures = license.getLicenseText().getCustomSignedFeatures();

        try {
            Map<SystemLicenseFeature, String> features = customSignedFeatures
                    .entrySet()
                    .stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(SystemLicenseFeature.valueOf(entry.getKey().toUpperCase()), entry.getValue()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            validateFeaturesFormat(features);

            return new SystemLicense(license, features);
        } catch (IllegalArgumentException e) {
            throw new InvalidSystemLicenseException(e.getMessage(), e);
        }
    }

    private void validateFeaturesFormat(Map<SystemLicenseFeature, String> features) throws IllegalSystemLicenseFormatException {
        features.entrySet().forEach(entry -> entry.getKey().validateFormat(entry.getValue()));
    }
}
