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
