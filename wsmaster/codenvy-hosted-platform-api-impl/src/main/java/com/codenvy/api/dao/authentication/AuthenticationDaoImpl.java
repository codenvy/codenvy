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

import com.google.common.base.Strings;

import org.eclipse.che.api.auth.AuthenticationDao;
import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.annotation.Nullable;
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
    @Inject
    protected UserDao                       userDao;

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


        AuthenticationHandler handler = handlerProvider.getDefaultHandler();

        String userId = handler.authenticate(credentials.getUsername(), credentials.getPassword());
        if (Strings.isNullOrEmpty(userId)) {
            LOG.error("Handler {} returned invalid userid during authentication of {}",
                      handler.getType(),
                      credentials.getUsername());
            throw new AuthenticationException("Provided username and password is not valid");
        }
        try {
            userDao.getById(userId);
        } catch (NotFoundException e) {
            LOG.warn("User {} is not found in the system. But {} successfully complete authentication",
                     credentials.getUsername(),
                     handler.getType());
            throw new AuthenticationException("Provided username and password is not valid");
        } catch (ServerException e) {
            LOG.warn("Fail to get user after authentication .User {} provider {} reason {} ",
                     credentials.getUsername(),
                     handler.getType(),
                     e.getLocalizedMessage());
            throw new AuthenticationException("Provided username and password is not valid");
        }
        // DO NOT REMOVE! This log will be used in statistic analyzing
        LOG.info("EVENT#user-sso-logged-in# USING#{}# USER#{}# ", handler.getType(), userId);
        Response.ResponseBuilder builder = Response.ok();
        if (tokenAccessCookie != null) {
            AccessTicket accessTicket = ticketManager.getAccessTicket(tokenAccessCookie.getValue());
            if (accessTicket != null) {
                if (!userId.equals(accessTicket.getUserId())) {
                    // DO NOT REMOVE! This log will be used in statistic analyzing
                    LOG.info("EVENT#user-changed-name# OLD-USER#{}# NEW-USER#{}#",
                             accessTicket.getUserId(),
                             userId);
                    LOG.info("EVENT#user-sso-logged-out# USER#{}#", accessTicket.getUserId());
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
        ticketManager.putAccessTicket(new AccessTicket(token, userId, handler.getType()));
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
                LOG.info("EVENT#user-sso-logged-out# USER#{}#", accessTicket.getUserId());
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
}
