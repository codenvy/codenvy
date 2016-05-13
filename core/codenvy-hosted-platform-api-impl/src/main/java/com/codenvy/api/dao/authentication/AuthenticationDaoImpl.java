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
package com.codenvy.api.dao.authentication;

import org.eclipse.che.api.auth.AuthenticationDao;
import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Authenticate user by username and password.
 *
 * <p>In response user receive "token". This token user can use
 * to identify him in all other request to API, to do that he should pass it as query parameter.
 *
 * @author Sergii Kabashniuk
 * @author Alexander Garagatyi
 */
public class AuthenticationDaoImpl implements AuthenticationDao {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationDaoImpl.class);

    @Inject
    protected AuthenticationHandlerProvider handlerProvider;
    @Inject
    protected TicketManager                 ticketManager;
    @Inject
    protected TokenGenerator                uniqueTokenGenerator;
    @Nullable
    @Inject
    protected CookieBuilder                 cookieBuilder;

    /**
     * Get token to be able to call secure api methods.
     *
     * @param tokenAccessCookie
     *         - old session-based cookie with token
     * @param credentials
     *         - username and password
     * @return - auth token in JSON, session-based and persistent cookies
     * @throws org.eclipse.che.api.auth.AuthenticationException
     */
    public Response login(Credentials credentials, Cookie tokenAccessCookie, UriInfo uriInfo) throws AuthenticationException {
        if (credentials == null
            || credentials.getPassword() == null
            || credentials.getPassword().isEmpty()
            || credentials.getUsername() == null
            || credentials.getUsername().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        boolean secure = uriInfo.getRequestUri().getScheme().equals("https");

        String realm = credentials.getRealm();

        AuthenticationHandler handler;
        if (realm == null) {
            handler = handlerProvider.getDefaultHandler();
        } else {
            handler = handlerProvider.getHandler(realm);
            if (handler == null) {
                throw new AuthenticationException("Unknown realm: " + realm);
            }
        }

        Subject principal = handler.authenticate(credentials.getUsername(), credentials.getPassword());
        if (principal == null) {
            throw new AuthenticationException("Provided user and password is not valid");
        }

        // DO NOT REMOVE! This log will be used in statistic analyzing
        LOG.info("EVENT#user-sso-logged-in# USING#{}# USER#{}# ", handler.getType(), principal.getUserName());
        Response.ResponseBuilder builder = Response.ok();
        if (tokenAccessCookie != null) {
            AccessTicket accessTicket = ticketManager.getAccessTicket(tokenAccessCookie.getValue());
            if (accessTicket != null) {
                if (!principal.equals(accessTicket.getPrincipal())) {
                    // DO NOT REMOVE! This log will be used in statistic analyzing
                    LOG.info("EVENT#user-changed-name# OLD-USER#{}# NEW-USER#{}#",
                             accessTicket.getPrincipal().getUserName(),
                             principal.getUserName());
                    LOG.info("EVENT#user-sso-logged-out# USER#{}#", accessTicket.getPrincipal().getUserName());
                    // DO NOT REMOVE! This log will be used in statistic analyzing
                    ticketManager.removeTicket(accessTicket.getAccessToken());
                }
            } else {
                //cookie is outdated, clearing
                if (cookieBuilder != null) {
                    cookieBuilder.clearCookies(builder, tokenAccessCookie.getValue(), secure);
                }

            }
        }
        // If we obtained principal  - authentication is done.
        String token = uniqueTokenGenerator.generate();
        ticketManager.putAccessTicket(new AccessTicket(token, principal, handler.getType()));
        if (cookieBuilder != null) {
            cookieBuilder.setCookies(builder, token, secure);
        }
        builder.entity(DtoFactory.getInstance().createDto(Token.class).withValue(token));
        return builder.build();
    }

    /**
     * Perform logout for the given token.
     *
     * @param token
     *         - authentication token
     * @param tokenAccessCookie
     *         - old session-based cookie with token.
     */
    public Response logout(String token, Cookie tokenAccessCookie, UriInfo uriInfo) {
        Response.ResponseBuilder response;
        String accessToken = token;
        if (accessToken == null && tokenAccessCookie != null) {
            accessToken = tokenAccessCookie.getValue();
        }

        boolean secure = uriInfo.getRequestUri().getScheme().equals("https");
        if (accessToken != null) {
            response = Response.ok();
            AccessTicket accessTicket = ticketManager.removeTicket(accessToken);
            if (accessTicket != null) {
                Subject subjectPrincipal = accessTicket.getPrincipal();
                // DO NOT REMOVE! This log will be used in statistic analyzing
                LOG.info("EVENT#user-sso-logged-out# USER#{}#", subjectPrincipal.getUserName());
            } else {
                LOG.warn("AccessTicket not found. Nothing to do.");
            }
        } else {
            response = Response.status(Response.Status.BAD_REQUEST);
            LOG.warn("Token not found in request.");
        }
        if (cookieBuilder != null) {
            cookieBuilder.clearCookies(response, accessToken, secure);
        }
        return response.build();
    }

//    public static class Credentials {
//        private String username;
//        private String password;
//
//        public Credentials() {
//        }
//
//        public Credentials(String username, String password) {
//            this.username = username;
//            this.password = password;
//        }
//
//        public String getUsername() {
//            return username;
//        }
//
//        public void setUsername(String username) {
//            this.username = username;
//        }
//
//        public String getPassword() {
//            return password;
//        }
//
//        public void setPassword(String password) {
//            this.password = password;
//        }
//    }
}
