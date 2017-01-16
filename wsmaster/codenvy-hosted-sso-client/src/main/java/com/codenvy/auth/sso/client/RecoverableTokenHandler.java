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
