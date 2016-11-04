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
package com.codenvy.license.shared;

import com.codenvy.license.shared.CodenvyLicense.LicenseType;

import java.text.ParseException;
import java.util.IllegalFormatException;

import static com.codenvy.license.shared.CodenvyLicense.EXPIRATION_DATE_FORMAT;
import static java.lang.String.format;

/**
 * Codenvy license custom features.
 */
public enum LicenseFeature {
    TYPE {
        @Override
        public Object parseValue(String value) {
            try {
                return LicenseType.valueOf(value.toUpperCase().replace(" ", "_"));
            } catch (IllegalFormatException e) {
                throw new IllegalLicenseFormatException(format("Unrecognizable Codenvy license. Unknown license type: '%s'", value));
            }
        }
    },
    EXPIRATION {
        @Override
        public Object parseValue(String value) {
            try {
                return EXPIRATION_DATE_FORMAT.parse(value);
            } catch (ParseException e) {
                throw new IllegalLicenseFormatException(
                        format("Unrecognizable Codenvy license. Invalid expiration date format: '%s'", value));
            }
        }
    },
    USERS {
        @Override
        public Object parseValue(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalLicenseFormatException(
                        format("Unrecognizable Codenvy license. Invalid number of users format: '%s'", value));
            }
        }
    };

    /**
     * Validates of License feature has appropriate format.
     */
    public void validateFormat(String value) throws IllegalLicenseFormatException {
        parseValue(value);
    }

    public abstract Object parseValue(String value);
}
