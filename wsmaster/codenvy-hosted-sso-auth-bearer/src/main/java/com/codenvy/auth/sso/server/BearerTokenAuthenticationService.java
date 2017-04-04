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
package com.codenvy.auth.sso.server;

import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.dao.authentication.CookieBuilder;
import com.codenvy.api.dao.authentication.TicketManager;
import com.codenvy.api.dao.authentication.TokenGenerator;
import com.codenvy.api.license.server.SystemLicenseManager;
import com.codenvy.auth.sso.server.email.template.VerifyEmailTemplate;
import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import com.codenvy.auth.sso.server.organization.UserCreationValidator;
import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.mail.EmailBean;
import com.codenvy.mail.MailSender;
import com.codenvy.template.processor.html.HTMLTemplateProcessor;
import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.codenvy.api.license.shared.model.Constants.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE;
import static com.codenvy.api.license.shared.model.Constants.UNABLE_TO_ADD_ACCOUNT_BECAUSE_OF_LICENSE;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

/**
 * Service to authenticate users using bearer tokens.
 *
 * @author Alexander Garagatyi
 * @author Sergey Kabashniuk
 */
@Path("internal/token")
public class BearerTokenAuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(BearerTokenAuthenticationService.class);

    private final TicketManager                            ticketManager;
    private final TokenGenerator                           uniqueTokenGenerator;
    private final BearerTokenAuthenticationHandler         handler;
    private final MailSender                               mailSender;
    private final EmailValidator                           emailValidator;
    private final CookieBuilder                            cookieBuilder;
    private final UserCreationValidator                    creationValidator;
    private final UserCreator                              userCreator;
    private final UserValidator                            userNameValidator;
    private final SystemLicenseManager                     licenseManager;
    private final DefaultEmailResourceResolver             resourceResolver;
    private final HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf;
    private final String                                   mailFrom;

    @Inject
    public BearerTokenAuthenticationService(TicketManager ticketManager,
                                            TokenGenerator uniqueTokenGenerator,
                                            BearerTokenAuthenticationHandler handler,
                                            MailSender mailSender,
                                            EmailValidator emailValidator,
                                            CookieBuilder cookieBuilder,
                                            UserCreationValidator creationValidator,
                                            UserCreator userCreator,
                                            UserValidator userNameValidator,
                                            SystemLicenseManager licenseManager,
                                            DefaultEmailResourceResolver resourceResolver,
                                            HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf,
                                            @Named("mailsender.application.from.email.address") String mailFrom) {
        this.ticketManager = ticketManager;
        this.uniqueTokenGenerator = uniqueTokenGenerator;
        this.handler = handler;
        this.mailSender = mailSender;
        this.emailValidator = emailValidator;
        this.cookieBuilder = cookieBuilder;
        this.creationValidator = creationValidator;
        this.userCreator = userCreator;
        this.userNameValidator = userNameValidator;
        this.licenseManager = licenseManager;
        this.resourceResolver = resourceResolver;
        this.thymeleaf = thymeleaf;
        this.mailFrom = mailFrom;
    }

    /**
     * Authenticates user by provided token, than creates the ldap user
     * and set the access/logged in cookies.
     *
     * @param credentials
     *         user credentials
     * @return principal user principal
     * @throws AuthenticationException
     */
    @POST
    @Path("authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticate(@CookieParam("session-access-key") Cookie tokenAccessCookie,
                                 Credentials credentials, @Context UriInfo uriInfo) throws AuthenticationException {

        if (handler == null) {
            LOG.warn("Bearer authenticator is null.");
            return Response.serverError().build();
        }

        boolean isSecure = uriInfo.getRequestUri().getScheme().equals("https");
        if (!handler.isValid(credentials.getToken())) {
            throw new AuthenticationException("Provided token is not valid");
        }
        Map<String, String> payload = handler.getPayload(credentials.getToken());
        handler.authenticate(credentials.getToken());

        try {
            final String username = userNameValidator.normalizeUserName(payload.get("username"));
            User user = userCreator.createUser(payload.get("email"), username, payload.get("firstName"), payload.get("lastName"));
            Response.ResponseBuilder builder = Response.ok();
            if (tokenAccessCookie != null) {
                AccessTicket accessTicket = ticketManager.getAccessTicket(tokenAccessCookie.getValue());
                if (accessTicket != null) {
                    if (!user.getId().equals(accessTicket.getUserId())) {
                        // DO NOT REMOVE! This log will be used in statistic analyzing
                        LOG.info("EVENT#user-changed-name# OLD-USER#{}# NEW-USER#{}#",
                                 accessTicket.getUserId(),
                                 user.getId());
                        LOG.info("EVENT#user-sso-logged-out# USER#{}#", accessTicket.getUserId());
                        // DO NOT REMOVE! This log will be used in statistic analyzing
                        ticketManager.removeTicket(accessTicket.getAccessToken());
                    }
                } else {
                    //cookie is outdated, clearing
                    cookieBuilder.clearCookies(builder, tokenAccessCookie.getValue(), isSecure);
                }
            }

            if (payload.containsKey("initiator")) {
                // DO NOT REMOVE! This log will be used in statistic analyzing
                LOG.info("EVENT#user-sso-logged-in# USING#{}# USER#{}#", payload.get("initiator"), username);
            }


            // If we obtained principal  - authentication is done.
            String token = uniqueTokenGenerator.generate();
            ticketManager.putAccessTicket(new AccessTicket(token, user.getId(), "bearer"));

            cookieBuilder.setCookies(builder, token, isSecure);
            builder.entity(Collections.singletonMap("token", token));

            LOG.debug("Authenticate user {} with token {}", username, token);
            return builder.build();
        } catch (IOException | ServerException e) {
            throw new AuthenticationException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Validates user email and user name, than sends confirmation mail.
     *
     * @param validationData
     *         - email and user name for validation
     * @param uriInfo
     * @return
     * @throws java.io.IOException
     */
    @POST
    @Path("validate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validate(ValidationData validationData, @Context UriInfo uriInfo)
            throws ApiException, IOException {

        String email = validationData.getEmail();

        emailValidator.validateUserMail(email);
        creationValidator.ensureUserCreationAllowed(email, validationData.getUsername());

        if (!licenseManager.isFairSourceLicenseAccepted()) {
            throw new ForbiddenException(FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE);
        }

        if (!licenseManager.canUserBeAdded()) {
            throw new ForbiddenException(UNABLE_TO_ADD_ACCOUNT_BECAUSE_OF_LICENSE);
        }
        final String bearerToken = handler.generateBearerToken(email,
                                                               validationData.getUsername(),
                                                               Collections.singletonMap("initiator", "email"));
        final String additionalParams = uriInfo.getRequestUri().getQuery();
        final String masterEndpoint = uriInfo.getBaseUriBuilder().replacePath(null).build().toString();
        final VerifyEmailTemplate emailTemplate = new VerifyEmailTemplate(bearerToken,
                                                                          additionalParams,
                                                                          masterEndpoint);
        mailSender.sendMail(resourceResolver.resolve(new EmailBean().withBody(thymeleaf.process(emailTemplate))
                                                                    .withFrom(mailFrom)
                                                                    .withTo(email)
                                                                    .withReplyTo(null)
                                                                    .withSubject("Verify Your Codenvy Account")
                                                                    .withMimeType(TEXT_HTML)));
        LOG.info("Email validation message send to {}", email);

        return Response.ok().build();
    }

    public static class ValidationData {
        private String email;
        private String userName;

        @SuppressWarnings("unused")
        public ValidationData() {}

        public ValidationData(String email, String userName) {
            this.email = email;
            this.userName = userName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUsername() {
            return userName;
        }

        public void setUsername(String userName) {
            this.userName = userName;
        }
    }

    public static class Credentials {
        private String token;

        @SuppressWarnings("unused")
        public Credentials() {}

        public Credentials(String username, String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
