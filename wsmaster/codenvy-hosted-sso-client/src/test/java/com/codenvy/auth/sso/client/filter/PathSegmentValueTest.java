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

public class PathSegmentValueTest {

    @Test(dataProvider = "skip")
    public void testShouldSkip(String requestUri) throws Exception {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080" + requestUri, null, 0, "GET", null);

        PathSegmentValueFilter filter = new PathSegmentValueFilter(4, "find");
        //when
        boolean result = filter.shouldSkip(request);
        //then
        Assert.assertTrue(result);


    }

    @Test(dataProvider = "notskip")
    public void testShouldNotSkip(String requestUri) throws Exception {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080" + requestUri, null, 0, "GET", null);

        PathSegmentValueFilter filter = new PathSegmentValueFilter(4, "organization");
        //when
        boolean result = filter.shouldSkip(request);
        //then
        Assert.assertFalse(result);


    }

    @DataProvider(name = "skip")
    public Object[][] skip() {
        return new Object[][]{{"/api/factory/ws3242oaidfa/find"},
                              {"/api/organization/ojo2934kpoak/find"}

        };
    }


    @DataProvider(name = "notskip")
    public Object[][] notskip() {
        return new Object[][]{{"/ws/_cloud-agent/2340sdf"},
                              {"/ws/myworkspace/_sso"},
                              {"/ws/myworkspace/_some/"},
                              {"/ws/myworkspace/_/"},
                              {"/ws/myworkspace/_git"},
                              {"/api/"},
                              {"/api/organization/ojo2934kpoak"},
                              {"/api/organization/ojo2934kpoak"},
                              {"/api/user/sdf02304"},
                              {"/api/user/find/"},
                              {"/api/user/sdf"},
                              {"/api/account/factoryixak9964p942mikq"}
        };
    }

}