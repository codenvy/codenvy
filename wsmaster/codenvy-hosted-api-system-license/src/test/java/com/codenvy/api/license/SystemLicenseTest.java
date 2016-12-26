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
import com.license4j.License;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 * @author Alexander Andrienko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SystemLicenseTest {

    @Mock
    private License license4j;

    public static final String EXPIRED_DATE     = "1990/12/31";
    public static final String NON_EXPIRED_DATE = "2100/12/31";
    public static final int    LICENSED_USERS   = 10;

    @BeforeMethod
    public void setUp() throws Exception {
        when(license4j.getLicenseString()).thenReturn("## (id: 123)\nabc");
    }

    @Test(dataProvider = "getDataToTestIsLicenseUsageLegal")
    public void testIsLicenseUsageLegal(String type, String expiration, int users, long actualUsers, int actualServers,
                                        boolean isLicenseUsageLegal) {
        Map<SystemLicenseFeature, String> features = ImmutableMap.of(SystemLicenseFeature.TYPE, type,
                                                                     SystemLicenseFeature.EXPIRATION, expiration,
                                                                     SystemLicenseFeature.USERS, String.valueOf(users));
        SystemLicense systemLicense = new SystemLicense(license4j, features);

        boolean result = systemLicense.isLicenseUsageLegal(actualUsers, actualServers);
        assertEquals(result, isLicenseUsageLegal);
    }

    @DataProvider
    public Object[][] getDataToTestIsLicenseUsageLegal() {
        return new Object[][]{
                // expired product key
                {SystemLicense.LicenseType.PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS, 0, 0, true},
                {SystemLicense.LicenseType.PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS, SystemLicense.MAX_NUMBER_OF_FREE_USERS,
                 SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {SystemLicense.LicenseType.PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS,
                 SystemLicense.MAX_NUMBER_OF_FREE_USERS + 1, SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},

                // non-expired product key
                {SystemLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, 0, 0, true},
                {SystemLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, LICENSED_USERS,
                 SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {SystemLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS,
                 SystemLicense.MAX_NUMBER_OF_FREE_USERS + 1, SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {SystemLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, LICENSED_USERS + 1,
                 SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},

                // expired evaluation product key
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS, 0, 0, true},
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS,
                 SystemLicense.MAX_NUMBER_OF_FREE_USERS, SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, LICENSED_USERS,
                 SystemLicense.MAX_NUMBER_OF_FREE_USERS + 1, SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},

                // non-expired evaluation product key
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, 0, 0, true},
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, LICENSED_USERS,
                 SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS,
                 SystemLicense.MAX_NUMBER_OF_FREE_USERS + 1, SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, true},
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, LICENSED_USERS, LICENSED_USERS + 1,
                 SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},
                };
    }

    @Test(dataProvider = "getDataToTestIsFreeUsageLegal")
    public void testIsFreeUsageLegal(long actualUsers, int actualServers, boolean isLicenseUsageLegal) {
        boolean result = SystemLicense.isFreeUsageLegal(actualUsers, actualServers);
        assertEquals(result, isLicenseUsageLegal);
    }

    @DataProvider
    public Object[][] getDataToTestIsFreeUsageLegal() {
        return new Object[][]{
                {0, 0, true},
                {SystemLicense.MAX_NUMBER_OF_FREE_USERS, SystemLicense.MAX_NUMBER_OF_FREE_SERVERS, true},
                {SystemLicense.MAX_NUMBER_OF_FREE_USERS, SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},
                {SystemLicense.MAX_NUMBER_OF_FREE_USERS + 1, SystemLicense.MAX_NUMBER_OF_FREE_SERVERS, false},
                {SystemLicense.MAX_NUMBER_OF_FREE_USERS + 1, SystemLicense.MAX_NUMBER_OF_FREE_SERVERS + 1, false},
                };
    }

    @Test(dataProvider = "getDataToTestIsLegalToAddNode")
          public void testIsLegalToAddNode(String type, String expiration, int actualServers, boolean isAddNodeLegal) {
        Map<SystemLicenseFeature, String> features = ImmutableMap.of(SystemLicenseFeature.TYPE, type, SystemLicenseFeature.EXPIRATION, expiration);
        SystemLicense systemLicense = new SystemLicense(license4j, features);

        boolean result = systemLicense.isLicenseNodesUsageLegal(actualServers);
        assertEquals(result, isAddNodeLegal);
    }

    @DataProvider
    public Object[][] getDataToTestIsLegalToAddNode() {
        return new Object[][]{
                {SystemLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, 0, true},
                {SystemLicense.LicenseType.PRODUCT_KEY.toString(), NON_EXPIRED_DATE, Integer.MAX_VALUE, true},
                {SystemLicense.LicenseType.PRODUCT_KEY.toString(), EXPIRED_DATE, 0, true},
                {SystemLicense.LicenseType.PRODUCT_KEY.toString(), EXPIRED_DATE, Integer.MAX_VALUE, true},

                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, 0, true},
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, 5, true},
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, 6, true},
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), NON_EXPIRED_DATE, Integer.MAX_VALUE, true},

                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, 0, true},
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, 1, true},
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, 5, true},
                {SystemLicense.LicenseType.EVALUATION_PRODUCT_KEY.toString(), EXPIRED_DATE, Integer.MAX_VALUE, true},
        };
    }
}
