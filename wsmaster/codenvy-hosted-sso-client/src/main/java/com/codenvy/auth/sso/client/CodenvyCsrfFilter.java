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
package com.codenvy.auth.sso.client;

import com.google.inject.Singleton;

import org.apache.catalina.filters.RestCsrfPreventionFilter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.codenvy.auth.sso.client.token.CookieRequestTokenExtractor.SECRET_TOKEN_ACCESS_COOKIE;

/**
 * Prevents <a href="https://en.wikipedia.org/wiki/Cross-site_request_forgery">CSRF</a>
 * attacks when cookie authentication is used.
 *
 * <p>Requires CSRF authentication header as specified
 * <a href="https://tomcat.apache.org/tomcat-7.0-doc/config/filter.html#CSRF_Prevention_Filter_for_REST_APIs">here</a>.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class CodenvyCsrfFilter implements Filter {

    private final RestCsrfPreventionFilter csrfPreventionFilter;

    @Inject
    public CodenvyCsrfFilter(@Named("csrf_filter.paths_accepting_parameters") String pathAcceptingParams) {
        csrfPreventionFilter = new RestCsrfPreventionFilter();
        csrfPreventionFilter.setPathsAcceptingParams(pathAcceptingParams);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        csrfPreventionFilter.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (containsSessionCookie(((HttpServletRequest)request).getCookies())) {
            csrfPreventionFilter.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean containsSessionCookie(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SECRET_TOKEN_ACCESS_COOKIE.equals(cookie.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        csrfPreventionFilter.destroy();
    }
}
