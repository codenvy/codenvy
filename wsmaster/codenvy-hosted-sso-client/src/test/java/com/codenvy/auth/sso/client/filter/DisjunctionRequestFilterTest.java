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

import org.mockito.Mock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/** @author Alexander Garagatyi */
public class DisjunctionRequestFilterTest {
    @Mock
    private HttpServletRequest request;

    @Test(dataProvider = "skip")
    public void testShouldSkip(List<RequestFilter> requestFilters) throws Exception {
        RequestFilter[] filters = (RequestFilter[])requestFilters.toArray();
        //given
        DisjunctionRequestFilter filter = new DisjunctionRequestFilter(filters[0], filters[1], Arrays.copyOfRange(
                filters, 2, filters.length));
        //when
        boolean result = filter.shouldSkip(request);
        //then
        Assert.assertTrue(result);
    }

    @Test(dataProvider = "notskip")
    public void testShouldNotSkip(List<RequestFilter> requestFilters) throws Exception {
        RequestFilter[] filters = (RequestFilter[])requestFilters.toArray();
        //given
        DisjunctionRequestFilter filter =
                new DisjunctionRequestFilter(filters[0], filters[1], Arrays.copyOfRange(filters, 2, filters.length));
        //when
        boolean result = filter.shouldSkip(request);
        //then
        Assert.assertFalse(result);
    }

    @DataProvider(name = "skip")
    public Object[][] skip() {
        return new Object[][]{{Arrays.asList(createFilter(true), createFilter(true))},
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
        return new Object[][]{{Arrays.asList(createFilter(false), createFilter(false))},
                              {Arrays.asList(createFilter(false), createFilter(false), createFilter(false))},
                              {Arrays.asList(createFilter(false), createFilter(false), createFilter(false), createFilter(false))}
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
