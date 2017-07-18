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
