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
package com.codenvy.auth.sso.client.filter;

import org.junit.Assert;

import org.everrest.test.mock.MockHttpServletRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static org.testng.Assert.*;

public class RequestMethodFilterTest {
    @Test(dataProvider = "skip")
    public void testShouldSkip(String requestUri, String method) throws Exception {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080" + requestUri, null, 0, method, null);

        RequestMethodFilter filter = new RequestMethodFilter("GET");
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

        RequestMethodFilter filter = new RequestMethodFilter("POST");
        //when
        boolean result = filter.shouldSkip(request);
        //then
        Assert.assertFalse(result);


    }

    @DataProvider(name = "notskip")
    public Object[][] notSkip() {
        return new Object[][]{{"/api/factory", "DELETE"},
                              {"/api/organization/ojo2934kpoak", "GET"},
                              {"/api/organization/ojo2934kpoak", "DELETE"},
                              {"/api/user", "GET"},
                              {"/api/user/sdf02304", "PUT"},
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