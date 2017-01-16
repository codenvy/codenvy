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
package com.codenvy.servlet;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Servlet applying "cache forever" HTTP headers.
 * <p/>
 * See: <a href="http://code.google.com/p/doctype/wiki/ArticleHttpCaching">ArticleHttpCaching</a>
 * <p/>
 */
@Singleton
public class CacheForcingFilter implements Filter {

    private static final long ONE_MONTH_IN_SECONDS = 60L * 60L * 24L * 30L;

    private static final long ONE_MONTH_IN_MILISECONDS = 1000L * ONE_MONTH_IN_SECONDS;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            Date now = new Date();
            httpResponse.setDateHeader("Date", now.getTime());
            httpResponse.setDateHeader("Expires", now.getTime() + ONE_MONTH_IN_MILISECONDS);
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Cache-control", "public, max-age=" + ONE_MONTH_IN_SECONDS);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
