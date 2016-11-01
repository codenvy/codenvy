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

import com.google.common.collect.ImmutableMap;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 * @author Alexander Andrienko
 */
public class CodenvyLicenseTest {

    public static final String EXPIRED_DATE     = "1990/12/31";
    public static final String NON_EXPIRED_DATE = "2100/12/31";
    public static final int    LICENSED_USERS   = 10;

    @Test(dataProvider = "getDataToTestIsLicenseUsageLegal")
    public void testIsLicenseUsageLegal(String type, String expiration, int users, long actualUsers, int actualServers,
                                        boolean isLicenseUsageLegal) {
        Map<LicenseFeature, String> features = ImmutableMap.of(LicenseFeature.TYPE, type,
                                                               LicenseFeature.EXPIRATION, expiration,
                                                               LicenseFeature.USERS, String.valueOf(users));
        CodenvyLicense license = new CodenvyLicense("", features);

        boolean result = license.isLicenseUsageLegal(actualUsers, actualServers);
        assertEquals(result, isLicenseUsageLegal);
    }

    @DataProvider
    public Object[][] getDataToTestIsLicenseUsageLegal() {
        return new Object[][]{
                // expired product key
                {CodenvyLicense.LicenseType.PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS, 0, 0, true},
                {CodenvyLicense.LicenseType.PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS, CodenvyLicense.MAX_NUMBER_OF_FREE_USERS,
                 CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {CodenvyLicense.LicenseType.PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS,
                 CodenvyLicense.MAX_NUMBER_OF_FREE_USERS + 1, CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},

                // non-expired product key
                {CodenvyLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, 0, 0, true},
                {CodenvyLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, LICENSED_USERS,
                 CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {CodenvyLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS,
                 CodenvyLicense.MAX_NUMBER_OF_FREE_USERS + 1, CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {CodenvyLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, LICENSED_USERS + 1,
                 CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},

                // expired evaluation product key
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS, 0, 0, true},
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS,
                 CodenvyLicense.MAX_NUMBER_OF_FREE_USERS, CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS,
                 CodenvyLicense.MAX_NUMBER_OF_FREE_USERS + 1, CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},

                // non-expired evaluation product key
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, 0, 0, true},
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, LICENSED_USERS,
                 CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS,
                 CodenvyLicense.MAX_NUMBER_OF_FREE_USERS + 1, CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, LICENSED_USERS + 1,
                 CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},
                };
    }

    @Test(dataProvider = "getDataToTestIsFreeUsageLegal")
    public void testIsFreeUsageLegal(long actualUsers, int actualServers, boolean isLicenseUsageLegal) {
        boolean result = CodenvyLicense.isFreeUsageLegal(actualUsers, actualServers);
        assertEquals(result, isLicenseUsageLegal);
    }

    @DataProvider
    public Object[][] getDataToTestIsFreeUsageLegal() {
        return new Object[][]{
                {0, 0, true},
                {CodenvyLicense.MAX_NUMBER_OF_FREE_USERS, CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS, true},
                {CodenvyLicense.MAX_NUMBER_OF_FREE_USERS, CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},
                {CodenvyLicense.MAX_NUMBER_OF_FREE_USERS + 1, CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS, false},
                {CodenvyLicense.MAX_NUMBER_OF_FREE_USERS + 1, CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},
                };
    }

    @Test(dataProvider = "getDataToTestIsLegalToAddNode")
          public void testIsLegalToAddNode(String type, String expiration, int actualServers, boolean isAddNodeLegal) {
        Map<LicenseFeature, String> features = ImmutableMap.of(LicenseFeature.TYPE, type, LicenseFeature.EXPIRATION, expiration);
        CodenvyLicense license = new CodenvyLicense("", features);

        boolean result = license.isLicenseNodesUsageLegal(actualServers);
        assertEquals(result, isAddNodeLegal);
    }

    @DataProvider
    public Object[][] getDataToTestIsLegalToAddNode() {
        return new Object[][]{
                {CodenvyLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, 0, true},
                {CodenvyLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, Integer.MAX_VALUE, true},
                {CodenvyLicense.LicenseType.PRODUCT_KEY.toString(), EXPIRED_DATE, 0, true},
                {CodenvyLicense.LicenseType.PRODUCT_KEY.toString(), EXPIRED_DATE, Integer.MAX_VALUE, true},

                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, 0, true},
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, 5, true},
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, 6, true},
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, Integer.MAX_VALUE, true},

                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, 0, true},
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, 1, true},
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, 5, true},
                {CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, Integer.MAX_VALUE, true},
        };
    }
}
