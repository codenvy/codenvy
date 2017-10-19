/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.auth.sso.client.filter;

import javax.servlet.http.HttpServletRequest;
import org.everrest.test.mock.MockHttpServletRequest;
import org.junit.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PathSegmentValueTest {

  @Test(dataProvider = "skip")
  public void testShouldSkip(String requestUri) throws Exception {
    // given
    HttpServletRequest request =
        new MockHttpServletRequest("http://localhost:8080" + requestUri, null, 0, "GET", null);

    PathSegmentValueFilter filter = new PathSegmentValueFilter(4, "find");
    // when
    boolean result = filter.shouldSkip(request);
    // then
    Assert.assertTrue(result);
  }

  @Test(dataProvider = "notskip")
  public void testShouldNotSkip(String requestUri) throws Exception {
    // given
    HttpServletRequest request =
        new MockHttpServletRequest("http://localhost:8080" + requestUri, null, 0, "GET", null);

    PathSegmentValueFilter filter = new PathSegmentValueFilter(4, "organization");
    // when
    boolean result = filter.shouldSkip(request);
    // then
    Assert.assertFalse(result);
  }

  @DataProvider(name = "skip")
  public Object[][] skip() {
    return new Object[][] {
      {"/api/factory/ws3242oaidfa/find"}, {"/api/organization/ojo2934kpoak/find"}
    };
  }

  @DataProvider(name = "notskip")
  public Object[][] notskip() {
    return new Object[][] {
      {"/ws/_cloud-agent/2340sdf"},
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
