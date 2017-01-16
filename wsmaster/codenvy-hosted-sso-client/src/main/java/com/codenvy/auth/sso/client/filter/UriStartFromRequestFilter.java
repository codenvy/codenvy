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
