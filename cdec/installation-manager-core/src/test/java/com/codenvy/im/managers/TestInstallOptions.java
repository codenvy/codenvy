/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.im.managers;

import com.google.common.collect.ImmutableMap;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public class TestInstallOptions {
    @DataProvider(name = "testCheckValidOptionsData")
    public static Object[][] CheckValidOptionsData() {
        return new Object[][]{
                {null, false},
                {new HashMap<String, String>() {{
                    put("some property", null);
                }}, false},
                {ImmutableMap.of("some property", ""), true},
                {ImmutableMap.of("some property", "MANDATORY"), false},
                {ImmutableMap.of("some property", "test", "property2", "MANDATORY"), false},
                {ImmutableMap.of("some property", "some value", "property 2", ""), true},
                {ImmutableMap.of("some property", "test"), true}
        };
    }

    @Test
    public void testSimpleEquals() {
        InstallOptions options = new InstallOptions().setStep(1);
        assertTrue(options.equals(options));
        assertFalse(options.equals(null));
        assertFalse(options.equals("string"));
    }

    @Test(dataProvider = "testEqualsAndHashCodeData")
    public void testEqualsAndHashCode(Map<String, String> properties1, int step1, InstallType type1,
                                      Map<String, String> properties2, int step2, InstallType type2,
                                      boolean expectedEquality) throws Exception {
        InstallOptions options1 =
                new InstallOptions().setStep(step1).setInstallType(type1).setConfigProperties(properties1);
        InstallOptions options2 =
                new InstallOptions().setStep(step2).setInstallType(type2).setConfigProperties(properties2);

        assertEquals(options1.equals(options2), expectedEquality);
        options1.hashCode();
    }

    @DataProvider(name = "testEqualsAndHashCodeData")
    public Object[][] TestEqualsAndHashCodeData() {
        return new Object[][]{
                {null, 0, null,
                 null, 0, null,
                 true},

                {ImmutableMap.of("1", "2"), 1, InstallType.SINGLE_SERVER,
                 ImmutableMap.of("1", "2"), 1, InstallType.SINGLE_SERVER,
                 true},

                {ImmutableMap.of("1", "2"), 0, null,
                 null, 0, null,
                 false},
                {ImmutableMap.of("1", "2"), 0, null,
                 ImmutableMap.of("3", "4"), 0, null,
                 false},
                {null, 0, null,
                 ImmutableMap.of("3", "4"), 0, null,
                 false},

                {ImmutableMap.of("1", "2"), 1, null,
                 ImmutableMap.of("1", "2"), 0, null,
                 false},
                {ImmutableMap.of("1", "2"), 0, InstallType.SINGLE_SERVER,
                 ImmutableMap.of("1", "2"), 0, null,
                 false},
                {ImmutableMap.of("1", "2"), 0, InstallType.SINGLE_SERVER,
                 ImmutableMap.of("1", "2"), 0, InstallType.MULTI_SERVER,
                 false},

                {ImmutableMap.of("1", "2"), 0, null,
                 ImmutableMap.of("1", "2"), 0, InstallType.MULTI_SERVER,
                 false},
        };
    }
}
