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

public class PathSegmentNumberFilterTest {

    @Test(dataProvider = "notskip")
    public void testShouldSkip(String requestUri) throws Exception {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080" + requestUri, null, 0, "GET", null);

        PathSegmentNumberFilter filter = new PathSegmentNumberFilter(3);
        //when
        boolean result = filter.shouldSkip(request);
        //then
        Assert.assertTrue(result);


    }

    @Test(dataProvider = "skip")
    public void testShouldNotSkip(String requestUri) throws Exception {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080" + requestUri, null, 0, "GET", null);

        PathSegmentNumberFilter filter = new PathSegmentNumberFilter(2);
        //when
        boolean result = filter.shouldSkip(request);
        //then
        Assert.assertFalse(result);


    }

    @DataProvider(name = "skip")
    public Object[][] skip() {
        return new Object[][]{{"/api/"},
                              {"/api/organization/ojo2934kpoak"},
                              {"/api/organization/ojo2934kpoak"},
                              //{"/api?user=sd"},
                              {"/api/user/sdf02304"},
                              {"/api/account/factoryixak9964p942mikq"}
        };
    }


    @DataProvider(name = "notskip")
    public Object[][] notskip() {
        return new Object[][]{{"/ws/_cloud-agent/2340sdf"},
                              {"/ws/myworkspace/_sso"},
                              {"/ws/myworkspace/_some/"},
                              {"/ws/myworkspace/_/"},
                              {"/ws/myworkspace/_git"}
        };
    }
}