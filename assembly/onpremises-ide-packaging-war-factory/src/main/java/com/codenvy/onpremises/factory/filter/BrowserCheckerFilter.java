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
package com.codenvy.onpremises.factory.filter;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Vitaliy Guliy
 */
@Singleton
public class BrowserCheckerFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;

        String userAgentHeader = request.getHeader("user-agent");
        if (userAgentHeader == null || userAgentHeader.trim().isEmpty()) {
            request.getRequestDispatcher("/resources/browser-not-supported.jsp").forward(request, response);
            return;
        }

        UserAgent userAgent = new UserAgent(userAgentHeader);

        // blocking Internet Explorer
        // Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)
        if (userAgent.hasProduct("Mozilla") &&
            userAgent.hasCommentPart("MSIE") &&
            userAgent.hasCommentPart("Windows") &&
            userAgent.hasCommentPart("Trident")) {
            request.getRequestDispatcher("/resources/browser-not-supported.jsp").forward(request, response);
            return;
        }

        // blocking Safari on Windows XP
        // Mozilla/5.0 (Windows NT 5.1) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2
        if (userAgent.hasProduct("AppleWebKit") &&
            userAgent.hasProduct("Version") &&
            userAgent.hasProduct("Safari") &&
            userAgent.hasCommentPart("Windows")) {
            request.getRequestDispatcher("/resources/browser-not-supported.jsp").forward(request, response);
            return;
        }

        // blocking Mobile browsers on Android 2
        // Mozilla/5.0 (Linux; U; Android 2.3.3; ru-ru; Fly_IQ280 Build/GINGERBREAD) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1
        if (userAgent.hasProduct("Mobile") &&
            userAgent.hasCommentPart("Android 2")) {
            request.getRequestDispatcher("/resources/browser-not-supported.jsp").forward(request, response);
            return;
        }

        chain.doFilter(req, resp);
    }
}
