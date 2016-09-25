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
package com.codenvy.im.utils;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class TestVersion {

    public static final String INVALID_VER = "invalid_version";

    @Test(dataProvider = "dataTestValidVersion")
    public void testValidVersion(String version) throws Exception {
        assertTrue(Version.isValidVersion(version), "Version is invalid: " + version);
    }

    @DataProvider
    public static Object[][] dataTestValidVersion() {
        return new Object[][] {{"0.0.1"},
                               {"1.0.1"},
                               {"10.3.0"},
                               {"10.3.0.0"},
                               {"10.3.0.1"},
                               {"10.3.0.1-RC1"},
                               {"0.9.0"},
                               {"1.0.0"},
                               {"1.0.10"},
                               {"1.0.1-GA"},
                               {"1.0.1-GA-SNAPSHOT"},
                               {"1.0.1-SNAPSHOT"},
                               {"1.0.1-RC10-SNAPSHOT"},
                               {"1.0.1.0-SNAPSHOT"},
                               {"1.0.1-M1"},
                               {"1.0.1.1-M1"},
                               {"1.0.1.1-M1-RC2"},
                               {"1.0.1-M1-SNAPSHOT"},
                               {"1.0.1.2-M1-SNAPSHOT"},
                               {"1.0.1.2-beta-10-SNAPSHOT"},
                               {"1.0.1.2-M1-beta-1-SNAPSHOT"},
                               {"1.0.1.2-M1-RC1-SNAPSHOT"}};
    }

    @Test(dataProvider = "dataTestInvalidVersion")
    public void testInvalidVersion(String version) throws Exception {
        assertFalse(Version.isValidVersion(version), "Version is valid: " + version);
    }

    @DataProvider
    public static Object[][] dataTestInvalidVersion() {
        return new Object[][] {{"1"},
                               {"00.1.1"},
                               {"1.1"},
                               {"1.1."},
                               {"1.1.1."},
                               {"1.01.1"},
                               {"01.1.1"},
                               {"1.1.01"},
                               {"1.0.1-"},
                               {"1.0.1-GA1"},
                               {"1.0.1-M"},
                               {"1.0.1-M0"},
                               {"1.0.1-M-SNAPSHOT"},
                               {"1.0.1-M0-SNAPSHOT"},
                               {"1.0.1-RC-SNAPSHOT"},
                               {"1.0.1-RC0-SNAPSHOT"},
                               {"1.0.1--SNAPSHOT"},
                               {"1.0.1-beta-0"},
                               {"1.0.1.2-RC1-beta-1-SNAPSHOT"},
                               {"1.0.1-SNAPSHOT-RC"}};
    }

    @Test(dataProvider = "dataTestParseValidVersion")
    public void testParseValidVersion(String str,
                                      int major,
                                      int minor,
                                      int bugFix,
                                      int hotFix,
                                      int milestone,
                                      int beta,
                                      int rc,
                                      boolean isGa,
                                      boolean isSnapshot) throws Exception {
        assertEquals(Version.valueOf(str), new Version(major, minor, bugFix, hotFix, milestone, beta, rc, isGa, isSnapshot));
    }


    @DataProvider
    public Object[][] dataTestParseValidVersion() {
        return new Object[][] {
                {"1.0.1-RC1", 1, 0, 1, 0, 0, 0, 1, false, false},
                {"1.0.1.0", 1, 0, 1, 0, 0, 0, 0, false, false},
                {"10.150.200.1", 10, 150, 200, 1, 0, 0, 0, false, false},
                {"10.150.200.24-SNAPSHOT", 10, 150, 200, 24, 0, 0, 0, false, true},
                {"10.150.200-M20", 10, 150, 200, 0, 20, 0, 0, false, false},
                {"10.150.200-M20-RC3", 10, 150, 200, 0, 20, 0, 3, false, false},
                {"10.150.200-M20-SNAPSHOT", 10, 150, 200, 0, 20, 0, 0, false, true},
                {"10.150.200-beta-10", 10, 150, 200, 0, 0, 10, 0, false, false},
                {"10.150.200-beta-1-SNAPSHOT", 10, 150, 200, 0, 0, 1, 0, false, true},
                {"10.150.200-RC1", 10, 150, 200, 0, 0, 0, 1, false, false},
                {"10.150.200-RC11-SNAPSHOT", 10, 150, 200, 0, 0, 0, 11, false, true},
                {"10.150.200.1-GA", 10, 150, 200, 1, 0, 0, 0, true, false},
                {"10.150.200.24-GA-SNAPSHOT", 10, 150, 200, 24, 0, 0, 0, true, true}};
    }


    @Test(expectedExceptions = IllegalVersionException.class)
    public void testParseInvalidVersion() throws Exception {
        Version.valueOf("01.1.1");
    }

    @Test(dataProvider = "dataTestToString")
    public void testToString(String str) throws Exception {
        assertEquals(Version.valueOf(str).toString(), str);
    }

    @DataProvider
    public Object[][] dataTestToString() {
        return new Object[][] {
                {"10.150.200"},
                {"10.150.200.1"},
                {"10.150.200.1-GA"},
                {"10.150.200.1-GA-SNAPSHOT"},
                {"10.150.200-M20-SNAPSHOT"},
                {"10.150.200.20-M20-SNAPSHOT"},
                {"10.150.200-M20"},
                {"10.150.200-SNAPSHOT"},
                {"10.150.200-beta-1"},
                {"10.150.200-RC1-SNAPSHOT"}};
    }


    @Test(dataProvider = "dataTestCompareTo")
    public void testCompareTo(String version1, String version2, int expectedCompareTo, boolean expectedGreaterThan) throws Exception {
        assertEquals(Version.valueOf(version1).compareTo(Version.valueOf(version2)), expectedCompareTo, version1 + " compareTo " + version2);
        assertEquals(Version.valueOf(version1).greaterThan(Version.valueOf(version2)), expectedGreaterThan, version1 + " greaterThan " + version2);
    }

    @DataProvider
    public Object[][] dataTestCompareTo() {
        return new Object[][] {
                {"1.0.1", "1.0.1", 0, false},
                {"1.0.1", "1.0.1.0", 0, false},
                {"1.0.2-M20", "1.0.2-M20", 0, false},
                {"1.0.2-M20-SNAPSHOT", "1.0.2-M20-SNAPSHOT", 0, false},
                {"1.0.2.4-M20-SNAPSHOT", "1.0.2.4-M20-SNAPSHOT", 0, false},
                {"1.0.2.4-RC1-SNAPSHOT", "1.0.2.4-RC1-SNAPSHOT", 0, false},
                {"1.0.2.4-GA-SNAPSHOT", "1.0.2.4-GA-SNAPSHOT", 0, false},
                {"1.0.2-SNAPSHOT", "1.0.2-SNAPSHOT", 0, false},

                {"4.0.1", "0.4.0", 1, true},
                {"2.0.1", "1.0.1", 1, true},
                {"1.1.1", "1.0.1", 1, true},
                {"1.0.1.1", "1.0.1", 1, true},
                {"1.0.1.1", "1.0.1.0", 1, true},
                {"1.0.2", "1.0.1", 1, true},
                {"1.0.2", "1.0.1-RC1", 1, true},
                {"1.0.2", "1.0.1-M20", 1, true},
                {"1.0.2", "1.0.2-SNAPSHOT", 1, true},
                {"1.0.2", "1.0.2-GA", 1, true},
                {"1.0.2-SNAPSHOT", "1.0.2-GA-SNAPSHOT", 1, true},
                {"1.0.2", "1.0.2-GA-SNAPSHOT", 1, true},
                {"1.0.2-GA", "1.0.2-GA-SNAPSHOT", 1, true},
                {"1.0.2-GA", "1.0.2-RC1", 1, true},
                {"1.0.2-GA-SNAPSHOT", "1.0.2-RC1", 1, true},
                {"1.0.2-GA", "1.0.2-RC1-SNAPSHOT", 1, true},
                {"1.0.2-GA-SNAPSHOT", "1.0.2-RC1-SNAPSHOT", 1, true},
                {"1.0.2-M20", "1.0.2-M19", 1, true},
                {"1.0.2-M20", "1.0.2-M20-SNAPSHOT", 1, true},
                {"1.0.2-M20", "1.0.2-M20-RC1", 1, true},
                {"1.0.2-beta-2", "1.0.2-beta-1", 1, true},
                {"1.0.2-RC2-SNAPSHOT", "1.0.2-RC1-SNAPSHOT", 1, true},
                {"1.0.2-RC1", "1.0.2-RC1-SNAPSHOT", 1, true},

                {"1.0.1", "2.0.1", -1, false},
                {"1.0.1", "1.1.1", -1, false},
                {"1.1.1.0", "1.1.1.1", -1, false},
                {"1.0.1", "1.0.2", -1, false},
                {"1.0.1-GA", "1.0.2-GA", -1, false},
                {"1.0.1-GA-SNAPSHOT", "1.0.1-GA", -1, false},
                {"1.0.1-RC2-SNAPSHOT", "1.0.1-GA-SNAPSHOT", -1, false},
                {"1.0.1-RC2", "1.0.1-GA", -1, false},
                {"1.0.1-SNAPSHOT", "1.0.1", -1, false},
                {"1.0.2-M20", "1.0.2", -1, false},
                {"1.0.2-beta-1", "1.0.2-RC1", -1, false}};
    }

    @Test(dataProvider = "dataTestIsSuitedFor")
    public void testIsSuitedFor(String version, String pattern, boolean expected) throws Exception {
        boolean actual = Version.valueOf(version).isSuitedFor(pattern);

        assertEquals(actual, expected, pattern);
    }

    @DataProvider
    public static Object[][] dataTestIsSuitedFor() {
        return new Object[][] {
                {"1.0.1", "1\\.0\\.1", true},
                {"1.0.1", "1\\.0\\.(.*)", true},
                {"1.0.1", "1\\.(.*)\\.1", true},
                {"1.0.1", "(.*)\\.0\\.1", true},
                {"1.0.1", "(.*)\\.(.*)\\.1", true},
                {"1.0.1", "(.*)\\.(.*)\\.(.*)", true},
                {"1.1.1-SNAPSHOT", "1\\.1\\.0|1\\.1\\.1\\-SNAPSHOT", true},
                {"1.1.1-beta-1", "1\\.1\\.(.*)", true},
                {"1.1.1-RC1", "1\\.1\\.(.*)", true},
                {"1.1.1-GA", "1\\.1\\.(.*)", true},
                {"1.1.1-GA-SNAPSHOT", "1\\.1\\.(.*)", true},
                {"1.1.1-M1", "1\\.1\\.(.*)", true},
                {"1.1.0", "1\\.1\\.0|1\\.\\1\\.1-SNAPSHOT", true},
                {"1.0.1", "1\\.0\\.2", false},
                {"1.0.1", "1\\.1\\.(.*)", false}
        };
    }

    @Test(dataProvider = "dataTestCertainIsMajor")
    public void testIsCertainMajor(boolean expectedIs3Major,
                                   boolean expectedIs4Major,
                                   boolean expectedIs4Compatible,
                                   boolean expectedIs5Major,
                                   String version) throws Exception {
        assertEquals(Version.valueOf(version).is3Major(), expectedIs3Major);
        assertEquals(Version.valueOf(version).is4Major(), expectedIs4Major);
        assertEquals(Version.valueOf(version).is4Compatible(), expectedIs4Compatible);
        assertEquals(Version.is4Compatible(version), expectedIs4Compatible);
        assertEquals(Version.valueOf(version).is5Major(), expectedIs5Major);
    }

    @DataProvider
    public static Object[][] dataTestCertainIsMajor() {
        return new Object[][]{
            {false, false, false, false, "2.0.0-RC1-SNAPSHOT"},
            {true, false, false, false, "3.0.4"},
            {false, true, true, false, "4.5.0-SNAPSHOT"},
            {false, false, true, true, "5.0.0-M1"},
            {false, false, false, false, "6.3.1-GA-SNAPSHOT"}
        };
    }
}
