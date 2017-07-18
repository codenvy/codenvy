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
import java.util.Collections;
import java.util.List;

/**
 * Filter LoginFilter request which uri start from the given list of patterns
 *
 * @author Sergii Kabashniuk
 */
public class UriStartFromRequestFilter implements RequestFilter {

    private final List<String> startUrlPatterns;

    public UriStartFromRequestFilter(List<String> startUrlPatterns) {
        this.startUrlPatterns = startUrlPatterns;
    }

    public UriStartFromRequestFilter(String startUrlPattern) {
        this.startUrlPatterns = Collections.singletonList(startUrlPattern);
    }

    @Override
    public boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String startUrlPattern : startUrlPatterns) {
            if (uri.startsWith(startUrlPattern))
                return true;
        }
        return false;
    }
}
