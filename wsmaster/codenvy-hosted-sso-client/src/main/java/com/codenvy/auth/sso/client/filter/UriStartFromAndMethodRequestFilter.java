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
 * Filter LoginFilter request by request method and beginning of the url.
 *
 * @author Sergii Kabashniuk
 */
public class UriStartFromAndMethodRequestFilter extends UriStartFromRequestFilter {
    private final String method;

    public UriStartFromAndMethodRequestFilter(String method, List<String> startUrlPatterns) {
        super(startUrlPatterns);
        this.method = method;
    }

    public UriStartFromAndMethodRequestFilter(String method, String startUrlPattern) {
        this(method, Collections.<String>singletonList(startUrlPattern));
    }

    @Override
    public boolean shouldSkip(HttpServletRequest request) {
        return method.equals(request.getMethod()) && super.shouldSkip(request);
    }
}
