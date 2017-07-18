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
import com.google.common.annotations.VisibleForTesting;
import com.license4j.License;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents valid system license.
 *
 * @author Anatoliy Bazko
 */
public class SystemLicense {
    private static final Pattern    LICENSE_ID_PATTERN                = Pattern.compile(".*\\(id: ([0-9]+)\\)");
    public static final  DateFormat EXPIRATION_DATE_FORMAT            = new SimpleDateFormat("yyyy/MM/dd");
    public static final  long       MAX_NUMBER_OF_FREE_USERS          = 3;
    public static final  int        ADDITIONAL_DAYS_FOR_LICENSE_RENEW = 15;

    private final Map<SystemLicenseFeature, String> features;
    private final License                           license4j;
    private final String                            id;

    SystemLicense(License license4j, Map<SystemLicenseFeature, String> features) {
        this.features = features;
        this.license4j = license4j;
        this.id = extractLicenseId();
    }

    public String getLicenseText() {
        return license4j.getLicenseString();
    }

    /**
     * @return unmodifiable list of system license features
     */
    public Map<SystemLicenseFeature, String> getFeatures() {
        return Collections.unmodifiableMap(features);
    }

    /**
     * Indicates if time for renew license expired.
     */
    public boolean isTimeForRenewExpired() {
        return getCurrentTime().after(getLicenseRenewExpirationDate());
    }

    /**
     * Indicates if license has been expired or hasn't, including additional days for license fix-up.
     */
    public boolean isExpired() {
        Date expirationDate = getExpirationDateFeatureValue();
        return getCurrentTime().after(expirationDate);
    }

    @VisibleForTesting
    Date getCurrentTime() {
        return Calendar.getInstance().getTime();
    }

    /**
     * Indicates if license is expired, but admin has additional time to renew it.
     */
    public boolean isExpiring() {
        return isExpired() && !isTimeForRenewExpired();
    }

    /**
     * Returns:
     * 1) days between current date and expiration date + additional time to renew it;
     * 2) 0 if time for license renew expired;
     * 3) -1 if license isn't expired.
     */
    public int daysBeforeTimeForRenewExpires() {
        if (isTimeForRenewExpired()) {
            return 0;
        }

        if (!isExpired()) {
            return -1;
        }

        int wholeDaysBeforeLicenseRenewExpires = Days.daysBetween(new DateTime(getCurrentTime()),
                                                                  new DateTime(getLicenseRenewExpirationDate())).getDays();
        return wholeDaysBeforeLicenseRenewExpires + 1;
    }

    private Date getLicenseRenewExpirationDate() {
        Date expirationDate = getExpirationDateFeatureValue();
        return new DateTime(expirationDate).plusDays(ADDITIONAL_DAYS_FOR_LICENSE_RENEW).toDate();
    }

    /**
     * @return {@link SystemLicenseFeature#EXPIRATION} feature value
     */
    public Date getExpirationDateFeatureValue() {
        return (Date)doGetFeature(SystemLicenseFeature.EXPIRATION);
    }

    /**
     * @return {@link SystemLicenseFeature#USERS} feature value
     */
    public int getNumberOfUsers() {
        return (int)doGetFeature(SystemLicenseFeature.USERS);
    }

    /**
     * @return {@link SystemLicenseFeature#TYPE} feature value
     */
    public LicenseType getLicenseType() {
        return (LicenseType)doGetFeature(SystemLicenseFeature.TYPE);
    }

    /**
     * @return true:
     * 1) if (EVALUATION_PRODUCT_KEY IS NOT expired) AND (actual number of users <= allowed by license)
     * 2) if (PRODUCT_KEY            IS NOT expired) AND (actual number of users <= allowed by license)
     * 3) if (EVALUATION_PRODUCT_KEY IS     expired) AND (actual number of users <= MAX_NUMBER_OF_FREE_USERS)
     * 4) if (PRODUCT_KEY            IS     expired) AND (actual number of users <= MAX_NUMBER_OF_FREE_USERS)
     */
    public boolean isLicenseUsageLegal(long actualUsers) {
        if (isTimeForRenewExpired()) {
            switch (getLicenseType()) {
                case EVALUATION_PRODUCT_KEY:
                case PRODUCT_KEY:
                    return actualUsers <= MAX_NUMBER_OF_FREE_USERS;   // don't take into account minimal free number of servers

                default:
                    return isFreeUsageLegal(actualUsers);
            }
        }

        return actualUsers <= getNumberOfUsers();
    }

    /**
     * Returns license id generated with license4j manager.
     */
    public String getLicenseId() {
        return id;
    }

    /**
     * @return false if (actual number of users > MAX_NUMBER_OF_FREE_USERS) OR (actual number of nodes > MAX_NUMBER_OF_FREE_SERVERS)
     */
    public static boolean isFreeUsageLegal(long actualUsers) {
        return actualUsers <= MAX_NUMBER_OF_FREE_USERS;
    }

    /**
     * Indicates if system license required activation.
     */
    public boolean isActivationRequired() {
        return license4j.isActivationRequired();
    }

    /**
     * Returns the origin of system license.
     * @see License
     */
    public License getOrigin() {
        return license4j;
    }

    private Object doGetFeature(SystemLicenseFeature feature) {
        return feature.parseValue(features.get(feature));
    }

    private String extractLicenseId() {
        Matcher matcher = LICENSE_ID_PATTERN.matcher(getLicenseText());
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new IllegalSystemLicenseFormatException("Unrecognized license format. Id not found");
    }

    /**
     * System license type.
     */
    public enum LicenseType {
        PRODUCT_KEY,
        EVALUATION_PRODUCT_KEY
    }
}
