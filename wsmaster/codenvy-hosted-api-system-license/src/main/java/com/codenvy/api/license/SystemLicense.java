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

import com.codenvy.api.license.exception.IllegalSystemLicenseFormatException;
import com.license4j.License;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    public static final  int        MAX_NUMBER_OF_FREE_SERVERS        = Integer.MAX_VALUE - 1;  // (-1) for testing propose only
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
     * Indicates if license has been expired or hasn't, including additional days for license fix-up.
     */
    public boolean isExpiredCompletely() {
        Date expirationDate = getExpirationDateFeatureValue();
        return expirationDate.before(DateTime.now().minusDays(ADDITIONAL_DAYS_FOR_LICENSE_RENEW).toDate());
    }

    /**
     * Indicates if license is expired, but admin has additional time to renew it.
     */
    public boolean isExpiring() {
        if (isExpiredCompletely()) {
            return false;
        }

        return daysBeforeCompleteExpiration() <= ADDITIONAL_DAYS_FOR_LICENSE_RENEW;
    }

    /**
     * Returns days between current date and expiration date + additional time torenew it, or 0 if license has already completely expired.
     */
    public int daysBeforeCompleteExpiration() {
        if (isExpiredCompletely()) {
            return 0;
        }

        int daysBeforeLicenseExpiredAccordingToItsFeatureValue = Days.daysBetween(new DateTime(System.currentTimeMillis()),
                                                                                  new DateTime(getExpirationDateFeatureValue())).getDays();

        return daysBeforeLicenseExpiredAccordingToItsFeatureValue + ADDITIONAL_DAYS_FOR_LICENSE_RENEW;
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
     * Returns true if user have order to create new node.
     */
    public boolean isLicenseNodesUsageLegal(int actualServers) {
        return true;
    }

    /**
     * @return true:
     * 1) if (EVALUATION_PRODUCT_KEY IS NOT expired) AND (actual number of users <= allowed by license)
     * 2) if (PRODUCT_KEY            IS NOT expired) AND (actual number of users <= allowed by license)
     * 3) if (EVALUATION_PRODUCT_KEY IS     expired) AND ((actual number of users <= MAX_NUMBER_OF_FREE_USERS) AND (number of nodes <= MAX_NUMBER_OF_FREE_SERVERS))
     * 4) if (PRODUCT_KEY            IS     expired) AND (actual number of users <= MAX_NUMBER_OF_FREE_USERS)
     */
    public boolean isLicenseUsageLegal(long actualUsers, int actualServers) {
        if (isExpiredCompletely()) {
            switch (getLicenseType()) {
                case EVALUATION_PRODUCT_KEY:
                case PRODUCT_KEY:
                    return actualUsers <= MAX_NUMBER_OF_FREE_USERS;   // don't take into account minimal free number of servers

                default:
                    return isFreeUsageLegal(actualUsers, actualServers);
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
    public static boolean isFreeUsageLegal(long actualUsers, int actualServers) {
        return actualUsers <= MAX_NUMBER_OF_FREE_USERS
               && actualServers <= MAX_NUMBER_OF_FREE_SERVERS;
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
