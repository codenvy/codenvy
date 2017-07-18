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

import com.google.inject.name.Named;

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

import static java.net.URLEncoder.encode;

/**
 * InvalidTokenHandler that user redirection to sso server or login page to recover authentication token.
 * Redirection used only on GET request.
 *
 * @author Sergii Kabashniuk
 */
public class RecoverableTokenHandler extends NoUserInteractionTokenHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RecoverableTokenHandler.class);

    @Inject
    @Named("auth.sso.client_allow_anonymous")
    protected boolean allowAnonymous;

    @Inject
    protected ClientUrlExtractor clientUrlExtractor;

    @Inject
    public RecoverableTokenHandler(RequestWrapper requestWrapper,
                                   ClientUrlExtractor clientUrlExtractor,
                                   @Named("auth.sso.client_allow_anonymous") boolean allowAnonymous) {
        super(requestWrapper);
        this.allowAnonymous = allowAnonymous;
        this.clientUrlExtractor = clientUrlExtractor;
    }

    @Override
    public void handleValidToken(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain chain,
                                 HttpSession session,
                                 SsoClientPrincipal principal) throws IOException, ServletException {
        if (!allowAnonymous && principal.getUser() != null && principal.getUser().isTemporary()) {
            LOG.warn("Anonymous user is not allowed on this client {} ", clientUrlExtractor.getClientUrl(request));
            handleBadToken(request, response, chain, principal.getToken());
        } else {
            super.handleValidToken(request, response, chain, session, principal);
        }
    }

    @Override
    public void handleBadToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String token)
            throws IOException, ServletException {

        if (!"GET".equals(request.getMethod())) {
            super.handleBadToken(request, response, chain, token);
        } else {
            sendUserToSSOServer(request, response);
        }
    }

    @Override
    public void handleMissingToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!"GET".equals(request.getMethod())) {
            super.handleMissingToken(request, response, chain);
        } else {
            sendUserToSSOServer(request, response);
        }
    }

    private void sendUserToSSOServer(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UriBuilder redirectUrl = UriBuilder.fromPath("/api/internal/sso/server/refresh");
        redirectUrl.queryParam("redirect_url", encode(UriBuilder.fromUri(request.getRequestURL().toString())
                                                                .replaceQuery(request.getQueryString())
                                                                .replaceQueryParam("token")
                                                                .build()
                                                                .toString(), "UTF-8"));
        redirectUrl.queryParam("client_url", encode(clientUrlExtractor.getClientUrl(request), "UTF-8"));
        redirectUrl.queryParam("allowAnonymous", allowAnonymous);
        response.sendRedirect(redirectUrl.build().toString());
    }

}
