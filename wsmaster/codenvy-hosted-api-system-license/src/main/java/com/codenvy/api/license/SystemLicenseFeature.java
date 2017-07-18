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

import com.codenvy.api.license.SystemLicense.LicenseType;
import com.codenvy.api.license.exception.IllegalSystemLicenseFormatException;

import java.text.ParseException;
import java.util.IllegalFormatException;

import static com.codenvy.api.license.SystemLicense.EXPIRATION_DATE_FORMAT;
import static java.lang.String.format;

/**
 * System license custom features.
 */
public enum SystemLicenseFeature {
    TYPE {
        @Override
        public Object parseValue(String value) {
            try {
                return LicenseType.valueOf(value.toUpperCase().replace(" ", "_"));
            } catch (IllegalFormatException e) {
                throw new IllegalSystemLicenseFormatException(format("Unrecognizable system license. Unknown license type: '%s'", value));
            }
        }
    },
    EXPIRATION {
        @Override
        public Object parseValue(String value) {
            try {
                return EXPIRATION_DATE_FORMAT.parse(value);
            } catch (ParseException e) {
                throw new IllegalSystemLicenseFormatException(
                        format("Unrecognizable system license. Invalid expiration date format: '%s'", value));
            }
        }
    },
    USERS {
        @Override
        public Object parseValue(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalSystemLicenseFormatException(
                        format("Unrecognizable system license. Invalid number of users format: '%s'", value));
            }
        }
    };

    /**
     * Validates of License feature has appropriate format.
     */
    public void validateFormat(String value) throws IllegalSystemLicenseFormatException {
        parseValue(value);
    }

    public abstract Object parseValue(String value);
}
