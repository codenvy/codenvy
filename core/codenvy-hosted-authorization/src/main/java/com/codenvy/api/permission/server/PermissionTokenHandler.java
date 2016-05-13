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
package com.codenvy.api.permission.server;

import com.codenvy.auth.sso.client.RolesContext;
import com.codenvy.auth.sso.client.ServerClient;
import com.codenvy.auth.sso.client.SsoClientPrincipal;
import com.codenvy.auth.sso.client.TokenHandler;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ForbiddenException;
import java.io.IOException;

import static java.lang.String.format;

/**
 * Sets up implementation of {@link Subject} that can check permissions by {@link PermissionChecker}
 * and delegates calls to injected {@link Named Named("delegated.handler")} {@link TokenHandler}
 *
 * @author Sergii Leschenko
 */
public class PermissionTokenHandler implements TokenHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PermissionTokenHandler.class);

    private final PermissionChecker permissionChecker;
    private final ServerClient      ssoServerClient;
    private final TokenHandler      delegate;

    @Inject
    public PermissionTokenHandler(PermissionChecker permissionChecker,
                                  ServerClient ssoServerClient,
                                  @Named("delegated.handler") TokenHandler delegate) {
        this.permissionChecker = permissionChecker;
        this.ssoServerClient = ssoServerClient;
        this.delegate = delegate;
    }

    @Override
    public void handleValidToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain, HttpSession session,
                                 RolesContext rolesContext, SsoClientPrincipal principal) throws IOException, ServletException {
        delegate.handleValidToken(request, response, chain, session, rolesContext,
                                  new SsoClientPrincipal(principal.getToken(),
                                                         principal.getClientUrl(),
                                                         rolesContext,
                                                         new AuthorizedSubject(principal.getUser(rolesContext)),
                                                         ssoServerClient));
    }

    @Override
    public void handleBadToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String token)
            throws IOException, ServletException {
        delegate.handleBadToken(request, response, chain, token);
    }

    @Override
    public void handleMissingToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        delegate.handleMissingToken(request, response, chain);
    }

    private class AuthorizedSubject implements Subject {
        private final Subject baseSubject;

        public AuthorizedSubject(Subject baseSubject) {
            this.baseSubject = baseSubject;
        }

        @Override
        public String getUserName() {
            return baseSubject.getUserName();
        }

        @Override
        public boolean isMemberOf(String role) {
            return baseSubject.isMemberOf(role);
        }

        @Override
        public boolean hasPermission(String domain, String instance, String action) {
            try {
                return permissionChecker.hasPermission(getUserId(), domain, instance, action);
            } catch (NotFoundException nfe) {
                return false;
            } catch (ServerException e) {
                LOG.error(format("Can't check permissions for user '%s' and instance '%s' of domain '%s'", getUserId(), domain, instance),
                          e);
                throw new RuntimeException("Can't check user's permissions", e);
            }
        }

        @Override
        public void checkPermission(String domain, String instance, String action) throws ForbiddenException {
            if (!hasPermission(domain, instance, action)) {
                throw new ForbiddenException("User is not authorized to perform " + action + " of " + domain
                                             + " with id '" + instance + "'");
            }
        }

        @Override
        public String getToken() {
            return baseSubject.getToken();
        }

        @Override
        public String getUserId() {
            return baseSubject.getUserId();
        }

        @Override
        public boolean isTemporary() {
            return baseSubject.isTemporary();
        }
    }
}
