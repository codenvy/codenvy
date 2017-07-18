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
package com.codenvy.auth.sso.client.filter;

import org.junit.Assert;

import org.everrest.test.mock.MockHttpServletRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

/** @author Sergii Kabashniuk */
public class ConjunctionRequestFilterTest {
    @Test(dataProvider = "skip")
    public void testShouldSkip(String requestUri, String method) throws Exception {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080" + requestUri, null, 0, method, null);

        ConjunctionRequestFilter filter = new ConjunctionRequestFilter(
                new UriStartFromRequestFilter("/api/factory"),
                new NegationRequestFilter(
                        new UriStartFromAndMethodRequestFilter("POST", "/api/factory"))
        );
        //when
        boolean result = filter.shouldSkip(request);
        //then
        Assert.assertTrue(result);


    }


    @Test(dataProvider = "notskip")
    public void testShouldNotSkip(String requestUri, String method) throws Exception {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080" + requestUri, null, 0, method, null);

        ConjunctionRequestFilter filter = new ConjunctionRequestFilter(
                new UriStartFromRequestFilter("/api/factory"),
                new NegationRequestFilter(
                        new UriStartFromAndMethodRequestFilter("POST", "/api/factory"))
        );
        //when
        boolean result = filter.shouldSkip(request);
        //then
        Assert.assertFalse(result);


    }

    @DataProvider(name = "notskip")
    public Object[][] notSkip() {
        return new Object[][]{{"/api/factory", "POST"},
                              {"/api/organization/ojo2934kpoak", "GET"},
                              {"/api/organization/ojo2934kpoak", "GET"},
                              {"/api/user", "GET"},
                              {"/api/user/sdf02304", "POST"},
                              {"/api/account/factoryixak9964p942mikq", "GET"}
        };
    }


    @DataProvider(name = "skip")
    public Object[][] skip() {
        return new Object[][]{{"/api/factory/factoryixak9964p942mikq/image?imgId=logo25d8d8sv58xcz8sd5xcz", "GET"},
                              {"/api/factory/factoryixak9964p942mikq/snippet?type=url", "GET"},
                              {"/api/factory/factoryixak9964p942mikq", "GET"}
        };
    }
}
