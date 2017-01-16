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
 * Servlet applying "never cache" HTTP headers.
 * <p/>
 * See:
 * <ul>
 * <li><a href="http://code.google.com/p/doctype/wiki/ArticleHttpCaching">ArticleHttpCaching</a></li>
 * <li><a href="http://seewah.blogspot.com/2009/02/gwt-tips-2-nocachejs-getting-cached-in.html">GWT Tips 2 - nocache.js getting cached in
 * browser</a></li>
 * </ul>
 * <p/>
 */
@Singleton
public class CacheDisablingFilter implements Filter {

    private static final long ONE_DAY_IN_MILISECONDS = (1000L * 60L * 60L * 24L);

    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            Date now = new Date();
            httpResponse.setDateHeader("Date", now.getTime());
            httpResponse.setDateHeader("Expires", now.getTime() + ONE_DAY_IN_MILISECONDS);
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
}
