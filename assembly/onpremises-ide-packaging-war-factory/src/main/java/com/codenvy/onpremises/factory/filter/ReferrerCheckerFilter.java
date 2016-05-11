/*
 *  [2012] - [2016] Codenvy, S.A.
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

import org.eclipse.che.api.factory.server.FactoryConstants;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.factory.shared.dto.Policies;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Validates that actual and factory object referrers are matching.
 *
 * @author Max Shaposhnik
 *
 */
@Singleton
public class ReferrerCheckerFilter implements Filter {

    @Inject
    @Named("page.invalid.factory")
    protected String INVALID_FACTORY_URL_PAGE;

    @Inject
    @Named("page.creation.error")
    protected String CREATION_FAILED_PAGE;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
            throws IOException, ServletException {

        Factory requestedFactory = (Factory)req.getAttribute("factory");
        if (requestedFactory == null) {
            dispatchToErrorPage(req, resp, CREATION_FAILED_PAGE, "Unable to get factory from request.");
        }

        String referrerHostName = null;
        final Policies policies = requestedFactory.getPolicies();
        if (policies != null && policies.getReferer() != null) {
            referrerHostName = policies.getReferer();
        }

        // header name typo is NOT a mistake. Do not change it.
        if (referrerHostName != null) {
            String referrer = ((HttpServletRequest)req).getHeader("Referer");
            String host = null;
            if (referrer != null) {
                try {
                    URI referrerUri = new URI(referrer);

                    // relative url
                    host = null == referrerUri.getHost() ? req.getServerName() : referrerUri.getHost();
                } catch (URISyntaxException e) {
                    dispatchToErrorPage(req, resp, INVALID_FACTORY_URL_PAGE, "Can't validate referer, can't parse hearer.");
                    return;
                }
            }

            if (host == null || (!referrerHostName.equals(host) && !host.matches(referrerHostName))) {
                dispatchToErrorPage(req, resp, INVALID_FACTORY_URL_PAGE, FactoryConstants.ILLEGAL_HOSTNAME_MESSAGE);
                return;
            }
        }
        filterChain.doFilter(req, resp);
    }

    protected void dispatchToErrorPage(ServletRequest req, ServletResponse resp, String dispatchPath, String message)
            throws ServletException, IOException {
        if (message != null) {
            req.setAttribute(RequestDispatcher.ERROR_MESSAGE, message);
        }
        req.getRequestDispatcher(dispatchPath).forward(req, resp);
    }
}
