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

import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.auth.sso.client.token.RequestTokenExtractor;

import org.eclipse.che.commons.subject.Subject;
import com.google.inject.name.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

import static java.net.URLEncoder.encode;

/**
 * Provide login redirection to SSO server on client side.
 * Filter also wraps original request and delegate Principal request to the Principal what comes from SSO server.
 *
 * @author Alexander Garagatyi
 * @author Sergey Kabashniuk
 * @author Andrey Parfonov
 */
@Singleton
public class LoginFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(LoginFilter.class);
    @Inject
    protected SessionStore          sessionStore;
    @Inject
    protected RequestTokenExtractor tokenExtractor;
    @Inject
    protected ClientUrlExtractor    clientUrlExtractor;
    @Inject
    protected SSOContextResolver    contextResolver;
    @Inject
    protected ServerClient          ssoServerClient;
    @Inject
    @Named("auth.sso.login_page_url")
    protected String                loginPageUrl;
    @Inject
    @Named("auth.sso.cookies_disabled_error_page_url")
    protected String                cookiesDisabledErrorPageUrl;
    @Inject
    protected RequestFilter         requestFilter;
    @Inject
    protected TokenHandler          tokenHandler;

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpReq = (HttpServletRequest)request;
        final HttpServletResponse httpResp = (HttpServletResponse)response;
        if (requestFilter.shouldSkip(httpReq)) {
            chain.doFilter(request, response);
            return;
        }

        final String method = httpReq.getMethod();
        final String clientUrl = clientUrlExtractor.getClientUrl(httpReq);

        // looks like we don't need client_url in queries, have to check it
        if ("GET".equals(method) && httpReq.getParameter("login") != null) {
            // forced sending user to login
            UriBuilder redirectUrlParameter =
                    UriBuilder.fromUri(httpReq.getRequestURL().toString()).replaceQuery(httpReq.getQueryString());
            redirectUrlParameter.replaceQueryParam("login");

            String redirectUrl = UriBuilder.fromUri(loginPageUrl)
                                           .queryParam("redirect_url",
                                                       encode(redirectUrlParameter.build().toString(), "UTF-8"))
                                           .queryParam("client_url", encode(clientUrl, "UTF-8")).build().toString();

            LOG.debug("Redirect to login {} ", redirectUrl);
            httpResp.sendRedirect(redirectUrl);
        } else {
            String token = tokenExtractor.getToken(httpReq);


            HttpSession session;
            if (token != null) {
                //TODO thread safety
                session = sessionStore.getSession(token);
                if (session == null) {
                    session = httpReq.getSession();
                    sessionStore.saveSession(token, session);
                }


                final RolesContext rolesContext = contextResolver.getRequestContext(httpReq);
                final SsoClientPrincipal principal = getPrincipal(session, token, clientUrl, rolesContext);
                if (principal == null || !principal.hasUserInContext(rolesContext)) {
                    tokenHandler.handleBadToken(httpReq, httpResp, chain, token);
                    return;
                }

                tokenHandler.handleValidToken(httpReq, httpResp, chain, session, rolesContext, principal);
                return;
            } else {
                //token not exists
                if (httpReq.getParameter("cookiePresent") != null) {
                    //we know that token have to be in cookies but it's not there
                    httpResp.sendRedirect(cookiesDisabledErrorPageUrl);
                } else {
                    tokenHandler.handleMissingToken(httpReq, httpResp, chain);
                    return;

                }
            }
        }
    }

    private SsoClientPrincipal getPrincipal(HttpSession session, String token, String clientUrl, RolesContext rolesContext) {
        SsoClientPrincipal principal = (SsoClientPrincipal)session.getAttribute("principal");
        if (principal == null || !principal.getToken().equals(token)) {
            // Case when same client use same http session but different authentication token
            if (principal != null) {
                sessionStore.removeSessionByToken(principal.getToken());
                sessionStore.saveSession(token, session);
            }
            Subject subject = ssoServerClient.getSubject(token, clientUrl, rolesContext.getWorkspaceId(), rolesContext.getAccountId());
            if (subject != null) {
                principal = new SsoClientPrincipal(token, clientUrl, rolesContext, subject, ssoServerClient);
                session.setAttribute("principal", principal);
            }
        }
        return principal;
    }

    @Override
    public void destroy() {
    }
}
