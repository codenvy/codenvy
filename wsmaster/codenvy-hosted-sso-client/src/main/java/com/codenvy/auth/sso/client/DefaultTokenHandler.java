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
                                 SsoClientPrincipal principal)
            throws IOException, ServletException {
        LOG.debug("User {} left filter with session id {} token {}", principal.getName(),
                  session.getId(), principal.getToken());
        if ("GET".equals(request.getMethod()) && request.getParameter("cookiePresent") != null) {
            response.sendRedirect(UriBuilder.fromUri(request.getRequestURL().toString())
                                            .replaceQuery(request.getQueryString())
                                            .replaceQueryParam("cookiePresent").build().toString());
        } else {
            try {
                Subject subject = principal.getUser();
                EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
                environmentContext.setSubject(subject);
                chain.doFilter(requestWrapper.wrapRequest(session, request, subject), response);
            } finally {
                EnvironmentContext.reset();
            }
        }
    }
}
