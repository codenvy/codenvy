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

import com.google.inject.Singleton;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Exclude LoginFilter request by regexp pattern;
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class RegexpRequestFilter implements RequestFilter {

    private final Pattern filterPattern;

    @Inject
    public RegexpRequestFilter(@Named("auth.sso.client_skip_filter_regexp") String filterPattern) {
        this.filterPattern = Pattern.compile(filterPattern);
    }

    @Override
    public boolean shouldSkip(HttpServletRequest request) {
        return filterPattern.matcher(request.getRequestURI()).matches();
    }
}
