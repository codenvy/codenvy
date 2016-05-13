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
package com.codenvy.auth.sso.server;

import com.codahale.metrics.annotation.Metered;
import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.dao.authentication.CookieBuilder;
import com.codenvy.api.dao.authentication.TicketManager;
import com.codenvy.api.dao.authentication.TokenGenerator;
import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.auth.sso.shared.dto.SubjectDto;

import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static java.net.URLEncoder.encode;

/**
 * Centralized authentication service that provide additional functionality.
 * Additional functionality is re-login after session destroying, anonymous login, invalidating some client in ticket
 * manager and retrieving user principal with roles from clients.
 *
 * @author Sergii Kabashnuk
 * @author Alexander Garagatyi
 */
@Path("internal/sso/server")
public class SsoService {
    private static final Logger LOG = LoggerFactory.getLogger(SsoService.class);

    private final TicketManager          ticketManager;
    private final RolesExtractorRegistry rolesExtractorRegistry;
    private final UserCreator            userCreator;
    private final TokenGenerator         uniqueTokenGenerator;
    private final CookieBuilder          cookieBuilder;
    private final String                 loginPage;

    @Inject
    public SsoService(TicketManager ticketManager,
                      RolesExtractorRegistry rolesExtractorRegistry,
                      UserCreator userCreator,
                      TokenGenerator uniqueTokenGenerator,
                      CookieBuilder cookieBuilder,
                      @Named("auth.sso.login_page_url") String loginPage) {
        this.ticketManager = ticketManager;
        this.rolesExtractorRegistry = rolesExtractorRegistry;
        this.userCreator = userCreator;
        this.uniqueTokenGenerator = uniqueTokenGenerator;
        this.cookieBuilder = cookieBuilder;
        this.loginPage = loginPage;
    }

    /**
     * Get principal with roles by token, and register user client on server.
     *
     * @throws AuthenticationException
     */
    @Metered(name = "auth.sso.service_get_token")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{token}")
    @GET
    public SubjectDto getCurrentPrincipal(@PathParam("token") String token,
                                       @QueryParam("clienturl") String clientUrl,
                                       @QueryParam("workspaceid") String workspaceId,
                                       @QueryParam("accountid") String accountId) throws AuthenticationException {

        LOG.debug("Request user  with token {}", token);

        if (clientUrl == null || clientUrl.isEmpty()) {
            throw new AuthenticationException("Mandatory parameter client url is not found");
        }

        AccessTicket accessTicket = ticketManager.getAccessTicket(token);
        if (accessTicket == null) {
            throw new AuthenticationException("Access token not found or expired.");
        } else {
            accessTicket.registerClientUrl(clientUrl);


            Set<String> roles = rolesExtractorRegistry.getRoles(accessTicket, workspaceId, accountId);
            return DtoFactory.newDto(SubjectDto.class)
                             .withName(accessTicket.getPrincipal().getUserName())
                             .withId(accessTicket.getPrincipal().getUserId())
                             .withRoles(new ArrayList<>(roles))
                             .withTemporary(roles.contains("temp_user"))
                             .withToken(accessTicket.getAccessToken());
        }
    }

    @Metered(name = "auth.sso.service_delete_token")
    @Path("{token}")
    @DELETE
    public void unregisterToken(@PathParam("token") String token,
                                @QueryParam("clienturl") String clientUrl) throws AuthenticationException {
        LOG.debug("Un-register token {} and client {} ", token, clientUrl);
        if (clientUrl == null || clientUrl.isEmpty()) {
            ticketManager.removeTicket(token);
        } else {
            AccessTicket accessTicket = ticketManager.getAccessTicket(token);
            if (accessTicket != null) {
                accessTicket.unRegisterClientUrl(clientUrl);
            }
        }
    }

    /**
     * Restore session cookie if persistent cookie is present, that allow re-login easily.
     * If there is no cookie user login as anonymous or gets error page if anonymous is restricted.
     *
     * @param redirectUrl
     *         - url for redirection after successful authentication.
     * @param tokenAccessCookie
     *         - cookie with authentication token
     * @param allowAnonymous
     *         - should service provide anonymous access if user is not authenticated.
     */
    @Metered(name = "auth.sso.service_refresh_token")
    @Path("refresh")
    @GET
    public Response authenticate(@QueryParam("redirect_url") String redirectUrl,
                                 @CookieParam("token-access-key") Cookie tokenAccessCookie,
                                 @QueryParam("allowAnonymous") String allowAnonymous,
                                 @Context UriInfo uriInfo) throws UnsupportedEncodingException {
        Response.ResponseBuilder builder;
        boolean isSecure = uriInfo.getRequestUri().getScheme().equals("https");
        try {
            if (tokenAccessCookie != null) {
                AccessTicket accessTicket = ticketManager.getAccessTicket(tokenAccessCookie.getValue());
                if (accessTicket != null && !(!Boolean.valueOf(allowAnonymous) && accessTicket.getPrincipal().isTemporary())) {

                    UriBuilder destination = UriBuilder.fromUri(redirectUrl);
                    destination.replaceQueryParam("cookiePresent", true);
                    builder = Response.temporaryRedirect(destination.build());

                    cookieBuilder.setCookies(builder, tokenAccessCookie.getValue(), isSecure);

                    return builder.build();
                }
            }

            if (Boolean.valueOf(allowAnonymous)) {
                // create new temp user
                final User user = userCreator.createTemporary();
                final Subject anonymousSubject = new SubjectImpl(user.getName(),
                                                                 user.getId(),
                                                                 null,
                                                                 Collections.<String>emptyList(),
                                                                 true);

                final AccessTicket ticket = new AccessTicket(uniqueTokenGenerator.generate(), anonymousSubject, "anonymous");
                ticketManager.putAccessTicket(ticket);

                UriBuilder destination = UriBuilder.fromUri(redirectUrl);
                destination.replaceQueryParam("cookiePresent", true);
                builder = Response.temporaryRedirect(destination.build());

                ((SsoCookieBuilder)cookieBuilder).setCookies(builder, ticket.getAccessToken(), isSecure, true);
            } else {
                builder = Response.temporaryRedirect(new URI(loginPage + "?redirect_url=" + encode(redirectUrl, "UTF-8")));
            }
        } catch (IOException | URISyntaxException e) {
            LOG.error(e.getLocalizedMessage(), e);
            builder = Response.serverError();
        }

        return builder.build();
    }
}
