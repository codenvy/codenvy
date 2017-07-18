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
