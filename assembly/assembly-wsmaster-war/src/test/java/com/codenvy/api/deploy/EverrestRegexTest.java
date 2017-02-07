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
package com.codenvy.api.deploy;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

/**
 * @author Michail Kuznyetsov
 */
public class EverrestRegexTest {
    private static final String REGEX = "^((?!(\\/(ws|eventbus)($|\\/.*)))\\/.*)";

    @Test(dataProvider = "shouldMatch")
    public void testShouldMatch(String path) {
        assertTrue(Pattern.matches(REGEX, path));
    }

    @Test(dataProvider = "shouldNotMatch")
    public void testShouldNotMatch(String path) {
        assertFalse(Pattern.matches(REGEX, path));
    }

    @DataProvider(name = "shouldMatch")
    public static Object[][] shouldMatch() {
        return new Object[][] {
                {"/wss"},
                {"/*"},
                {"/querry?param=1;"},
                {"/"},
        };
    }

    @DataProvider(name = "shouldNotMatch")
    public static Object[][] shouldNotMatch() {
        return new Object[][] {
                {"ws"},
                {"/ws"},
                {"/ws/"},
                {"/ws/*"},
                {"/eventbus"},
                {"/eventbus/"},
                {"/eventbus/*"},
                {"/ws/querry?param=1;"},
        };
    }

}
