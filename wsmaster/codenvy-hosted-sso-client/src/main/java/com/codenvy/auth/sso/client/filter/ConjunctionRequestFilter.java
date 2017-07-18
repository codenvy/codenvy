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
