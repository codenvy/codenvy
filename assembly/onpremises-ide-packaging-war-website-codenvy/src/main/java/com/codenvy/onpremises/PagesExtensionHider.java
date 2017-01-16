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
package com.codenvy.onpremises;

import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Allow to hide file html extension from the user on the site. */
@Singleton
public class PagesExtensionHider implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(PagesExtensionHider.class);

    private final Pattern excludesPattern;

    @Inject
    public PagesExtensionHider(@Named("pagehider.exclude.regexppattern") @Nullable String excludesPattern) {
        this.excludesPattern = excludesPattern == null ? null : Pattern.compile(excludesPattern);
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;

        String requestURI = httpRequest.getServletPath();
        LOG.debug("Request {} to {} ", httpRequest.getMethod(), requestURI);

        //most commonly used request to main page.
        if ("/".equals(requestURI)) {
            chain.doFilter(request, response);
        }
        //request not to pages or resources
        else if (!"GET".equals(httpRequest.getMethod())) {
            chain.doFilter(request, response);
        }
        //to the index page o
        else if ("/".equals(requestURI.substring(requestURI.length() - 1))) {
            chain.doFilter(request, response);
        }
        //non page resources of the site like scripts, css, fonts
        else if (requestURI.contains(".")) {
            chain.doFilter(request, response);
        }
        //known mapped services
        else {
            if (excludesPattern != null) {
                Matcher m = excludesPattern.matcher(requestURI);
                if (m.matches()) {
                    chain.doFilter(request, response);
                    return;
                }
            }
            //request to common site pages
            httpRequest.getRequestDispatcher(requestURI + ".html").forward(request, response);
        }
    }

    @Override
    public void destroy() {
    }

}
