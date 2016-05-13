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
package com.codenvy.auth.sso.client;

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

/**
 * Reference implementation that handle valid token usecase.
 *
 * @author Sergii Kabashniuk
 */
public abstract class DefaultTokenHandler implements TokenHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultTokenHandler.class);

    protected final RequestWrapper requestWrapper;

    @Inject
    protected DefaultTokenHandler(RequestWrapper requestWrapper) {
        this.requestWrapper = requestWrapper;
    }

    @Override
    public void handleValidToken(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain chain,
                                 HttpSession session,
                                 RolesContext rolesContext,
                                 SsoClientPrincipal principal)
            throws IOException, ServletException {
        LOG.debug("User {} left filter with session id {} token {}", principal.getName(),
                  session.getId(), principal.getToken());
        if ("GET".equals(request.getMethod()) && request.getParameter("cookiePresent") != null) {
            response.sendRedirect(UriBuilder.fromUri(request.getRequestURL().toString())
                                            .replaceQuery(request.getQueryString())
                                            .replaceQueryParam("cookiePresent").build().toString());
        } else {
            Subject subject = principal.getUser(rolesContext);
            EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
            environmentContext.setSubject(subject);
            chain.doFilter(requestWrapper.wrapRequest(session, request, subject), response);
        }
    }
}
