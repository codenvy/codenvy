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

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.mockito.Mock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
public class DisjunctionRequestFilterTest {
  @Mock private HttpServletRequest request;

  @Test(dataProvider = "skip")
  public void testShouldSkip(List<RequestFilter> requestFilters) throws Exception {
    RequestFilter[] filters = (RequestFilter[]) requestFilters.toArray();
    //given
    DisjunctionRequestFilter filter =
        new DisjunctionRequestFilter(
            filters[0], filters[1], Arrays.copyOfRange(filters, 2, filters.length));
    //when
    boolean result = filter.shouldSkip(request);
    //then
    Assert.assertTrue(result);
  }

  @Test(dataProvider = "notskip")
  public void testShouldNotSkip(List<RequestFilter> requestFilters) throws Exception {
    RequestFilter[] filters = (RequestFilter[]) requestFilters.toArray();
    //given
    DisjunctionRequestFilter filter =
        new DisjunctionRequestFilter(
            filters[0], filters[1], Arrays.copyOfRange(filters, 2, filters.length));
    //when
    boolean result = filter.shouldSkip(request);
    //then
    Assert.assertFalse(result);
  }

  @DataProvider(name = "skip")
  public Object[][] skip() {
    return new Object[][] {
      {Arrays.asList(createFilter(true), createFilter(true))},
      {Arrays.asList(createFilter(true), createFilter(false))},
      {Arrays.asList(createFilter(false), createFilter(true))},
      {Arrays.asList(createFilter(true), createFilter(true), createFilter(false))},
      {Arrays.asList(createFilter(true), createFilter(false), createFilter(true))},
      {Arrays.asList(createFilter(false), createFilter(true), createFilter(true))},
      {Arrays.asList(createFilter(false), createFilter(false), createFilter(true))},
      {Arrays.asList(createFilter(false), createFilter(true), createFilter(false))},
      {Arrays.asList(createFilter(true), createFilter(false), createFilter(false))}
    };
  }

  @DataProvider(name = "notskip")
  public Object[][] notSkip() {
    return new Object[][] {
      {Arrays.asList(createFilter(false), createFilter(false))},
      {Arrays.asList(createFilter(false), createFilter(false), createFilter(false))},
      {
        Arrays.asList(
            createFilter(false), createFilter(false), createFilter(false), createFilter(false))
      }
    };
  }

  private RequestFilter createFilter(final boolean result) {
    return new RequestFilter() {
      @Override
      public boolean shouldSkip(HttpServletRequest request) {
        return result;
      }
    };
  }
}
