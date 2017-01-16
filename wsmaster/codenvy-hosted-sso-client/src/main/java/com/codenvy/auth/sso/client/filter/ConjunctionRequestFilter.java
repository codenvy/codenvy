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

import javax.servlet.http.HttpServletRequest;

/**
 * Return result of conjunction of two filters.
 *
 * @author Sergii Kabashniuk
 */
public class ConjunctionRequestFilter implements RequestFilter {
    private final RequestFilter[] requestFilters;

    public ConjunctionRequestFilter(RequestFilter requestFilterA, RequestFilter requestFilterB, RequestFilter... anotherRequestFilters) {
        this.requestFilters = new RequestFilter[anotherRequestFilters.length + 2];
        this.requestFilters[0] = requestFilterA;
        this.requestFilters[1] = requestFilterB;
        System.arraycopy(anotherRequestFilters, 0, this.requestFilters, 2, anotherRequestFilters.length);
    }

    @Override
    public boolean shouldSkip(HttpServletRequest request) {
        boolean result = true;
        for (int i = 0; result && i < requestFilters.length; i++) {
            result = requestFilters[i].shouldSkip(request);
        }
        return result;
    }
}
