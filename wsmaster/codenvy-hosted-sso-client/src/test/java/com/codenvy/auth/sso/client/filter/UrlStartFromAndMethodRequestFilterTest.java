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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/** @author Sergii Kabashniuk */
public class UrlStartFromAndMethodRequestFilterTest {

    private String filterPattern = "^/ws/([^/]+/_sso|_cloud-agent)/(.+)$";

    @Test(dataProvider = "excludedPathProvider")
    public void shouldSkipRequestWithExcludedPath(String path) throws IOException, ServletException {
        //given
        HttpServletRequest request = new MockHttpServletRequest("http://localhost:8080" + path, null, 0, "GET", null);

        RegexpRequestFilter filter = new RegexpRequestFilter(filterPattern);
        //when -then
        Assert.assertTrue(filter.shouldSkip(request));

    }

    @DataProvider(name = "excludedPathProvider")
    public Object[][] excludedPathProvider() {
        return new Object[][]{{"/ws/_cloud-agent/statistics"},
                              {"/ws/myworkspace/_sso/client/updateroles"}
        };
    }

    @DataProvider(name = "notExcludedPathProvider")
    public Object[][] notExcludedPathProvider() {
        return new Object[][]{{"/ws/_cloud-agent"},
                              {"/ws/myworkspace/_sso"},
                              {"/ws/myworkspace/_some/path"},
                              {"/ws/myworkspace/_/some/path"},
                              {"/ws/myworkspace/_git"}
        };
    }

    @Test(dataProvider = "notExcludedPathProvider")
    public void shouldNotSkipRequestWithNotExcludedPath(String path) throws IOException, ServletException {
        //given
        HttpServletRequest request = new MockHttpServletRequest("http://localhost:8080" + path, null, 0, "GET", null);
        RegexpRequestFilter filter = new RegexpRequestFilter(filterPattern);
        //when -then
        Assert.assertFalse(filter.shouldSkip(request));

    }
}
