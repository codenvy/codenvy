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
package com.codenvy.service.password;

import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.mail.EmailBean;
import com.codenvy.mail.MailSender;
import com.codenvy.service.password.email.template.PasswordRecoveryTemplate;
import com.codenvy.template.processor.html.HTMLTemplateProcessor;
import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.user.server.ProfileManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.TEXT_HTML;

/**
 * Services for password features
 *
 * @author Michail Kuznyetsov
 */
@Path("/password")
public class PasswordService {

    private static final Logger LOG           = LoggerFactory.getLogger(PasswordService.class);
    private static final String MAIL_TEMPLATE = "/email-templates/password_recovery.html";

    private final MailSender                               mailService;
    private final UserManager                              userDao;
    private final ProfileManager                           profileManager;
    private final RecoveryStorage                          recoveryStorage;
    private final DefaultEmailResourceResolver             resourceResolver;
    private final String                                   mailFrom;
    private final String                                   recoverMailSubject;
    private final HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf;
    private final long                                     validationMaxAge;

    @Context
    private UriInfo uriInfo;

    @Inject
    public PasswordService(MailSender mailSender,
                           UserManager userManager,
                           RecoveryStorage recoveryStorage,
                           ProfileManager profileManager,
                           DefaultEmailResourceResolver resourceResolver,
                           @Named("mailsender.application.from.email.address") String mailFrom,
                           @Named("password.recovery.mail.subject") String recoverMailSubject,
                           HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf,
                           @Named("password.recovery.expiration_timeout_hours") long validationMaxAge) {
        this.recoveryStorage = recoveryStorage;
        this.mailService = mailSender;
        this.userDao = userManager;
        this.profileManager = profileManager;
        this.resourceResolver = resourceResolver;
        this.mailFrom = mailFrom;
        this.recoverMailSubject = recoverMailSubject;
        this.thymeleaf = thymeleaf;
        this.validationMaxAge = validationMaxAge;
    }

    /**
     * Sends mail for password restoring
     * <p/>
     * <table>
     * <tr>
     * <th>Status</th>
     * <th>Error description</th>
     * </tr>
     * <tr>
     * <td>404</td>
     * <td>specified user is not registered</td>
     * </tr>
     * <tr>
     * <td>500</td>
     * <td>problem with user database</td>
     * </tr>
     * <tr>
     * <td>500</td>
     * <td>problems on email sending</td>
     * </tr>
     * </table>
     *
     * @param mail
     *         the identifier of user
     */
    @POST
    @Path("recover/{usermail}")
    public void recoverPassword(@PathParam("usermail") String mail) throws ServerException, NotFoundException {
        try {
            //check if user exists
            userDao.getByEmail(mail);
            final String masterEndpoint = uriInfo.getBaseUriBuilder()
                                                 .replacePath(null)
                                                 .build()
                                                 .toString();
            final String tokenAgeMessage = String.valueOf(validationMaxAge) + " hour";
            final String uuid = recoveryStorage.generateRecoverToken(mail);
            final String body = thymeleaf.process(new PasswordRecoveryTemplate(tokenAgeMessage,
                                                                               masterEndpoint,
                                                                               uuid));
            mailService.sendMail(resourceResolver.resolve(new EmailBean().withBody(body)
                                                                         .withFrom(mailFrom)
                                                                         .withTo(mail)
                                                                         .withReplyTo(null)
                                                                         .withSubject(recoverMailSubject)
                                                                         .withMimeType(TEXT_HTML)));
        } catch (NotFoundException e) {
            throw new NotFoundException("User " + mail + " is not registered in the system.");
        } catch (ApiException e) {
            LOG.error("Error during setting user's password", e);
            throw new ServerException("Unable to recover password. Please contact support or try later.");
        }
    }

    /**
     * Verify setup password confirmation token.
     * <p/>
     * <table>
     * <tr>
     * <th>Status</th>
     * <th>Error description</th>
     * </tr>
     * <tr>
     * <td>403</td>
     * <td>Setup password token is incorrect or has expired</td>
     * </tr>
     * </table>
     *
     * @param uuid
     *         token of setup password operation
     */
    @GET
    @Path("verify/{uuid}")
    public void setupConfirmation(@PathParam("uuid") String uuid) throws ForbiddenException {
        if (!recoveryStorage.isValid(uuid)) {
            // remove invalid validationData
            recoveryStorage.remove(uuid);

            throw new ForbiddenException("Setup password token is incorrect or has expired");
        }
    }

    /**
     * Setup users password after verifying setup password confirmation token
     * <p/>
     * <table>
     * <tr>
     * <th>Status</th>
     * <th>Error description</th>
     * </tr>
     * <tr>
     * <td>403</td>
     * <td>Setup password token is incorrect or has expired</td>
     * </tr>
     * <tr>
     * <td>404</td>
     * <td>User is not registered in the system</td>
     * </tr>
     * <tr>
     * <td>500</td>
     * <td>Impossible to setup password</td>
     * </tr>
     * <p/>
     * <p/>
     * </table>
     *
     * @param uuid
     *         token of setup password operation
     * @param newPassword
     *         new users password
     */
    @POST
    @Path("setup")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void setupPassword(@FormParam("uuid") String uuid, @FormParam("password") String newPassword)
            throws NotFoundException, ServerException, ConflictException, ForbiddenException {
        // verify is confirmationId valid
        if (!recoveryStorage.isValid(uuid)) {
            // remove invalid validationData
            recoveryStorage.remove(uuid);

            throw new ForbiddenException("Setup password token is incorrect or has expired");
        }


        // find user and setup his/her password
        String email = recoveryStorage.get(uuid);

        try {
            final UserImpl user = new UserImpl(userDao.getByEmail(email));
            user.setPassword(newPassword);
            userDao.update(user);

            final Profile profile = profileManager.getById(user.getId());
            if (profile.getAttributes().remove("resetPassword") != null) {
                profileManager.update(profile);
            }
        } catch (NotFoundException e) {
            // remove invalid validationData
            throw new NotFoundException("User " + email + " is not registered in the system.");
        } catch (ServerException e) {
            LOG.error("Error during setting user's password", e);
            throw new ServerException("Unable to setup password. Please contact support.");
        } finally {
            // remove validation data from validationStorage
            recoveryStorage.remove(uuid);
        }
    }
}
