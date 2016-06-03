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
package com.codenvy.service.password;

import com.codenvy.mail.MailSenderClient;
import com.codenvy.mail.shared.dto.AttachmentDto;
import com.codenvy.mail.shared.dto.EmailBeanDto;
import com.google.common.io.Files;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserProfileDao;

import org.eclipse.che.commons.lang.Deserializer;
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
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.eclipse.che.commons.lang.IoUtil.getResource;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Services for password features
 *
 * @author Michail Kuznyetsov
 */
@Path("/password")
public class PasswordService {

    private static final Logger LOG           = LoggerFactory.getLogger(PasswordService.class);
    private static final String MAIL_TEMPLATE = "/email-templates/password_recovery.html";
    private static final String LOGO          = "/email-templates/header.png";
    private static final String LOGO_CID      =  "codenvyLogo";

    private final MailSenderClient mailService;
    private final UserDao          userDao;
    private final UserProfileDao   userProfileDao;
    private final RecoveryStorage  recoveryStorage;
    private final String           mailSender;
    private final String           recoverMailSubject;
    private final long             validationMaxAge;

    @Context
    private UriInfo uriInfo;

    @Inject
    public PasswordService(MailSenderClient mailService,
                           UserDao userDao,
                           RecoveryStorage recoveryStorage,
                           UserProfileDao userProfileDao,
                           @Named("mailsender.application.from.email.address") String mailSender,
                           @Named("password.recovery.mail.subject") String recoverMailSubject,
                           @Named("password.recovery.expiration_timeout_hours") long validationMaxAge) {
        this.recoveryStorage = recoveryStorage;
        this.mailService = mailService;
        this.userDao = userDao;
        this.userProfileDao = userProfileDao;
        this.mailSender = mailSender;
        this.recoverMailSubject = recoverMailSubject;
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
    public void recoverPassword(@PathParam("usermail") String mail)
            throws ServerException, NotFoundException {
        try {
            //check if user exists
            userDao.getByAlias(mail);

            String uuid = recoveryStorage.generateRecoverToken(mail);

            Map<String, String> props = new HashMap<>();
            props.put("logo.cid", LOGO_CID);
            props.put("id", uuid);
            props.put("validation.token.age.message", String.valueOf(validationMaxAge) + " hour");
            props.put("com.codenvy.masterhost.url", uriInfo.getBaseUriBuilder().replacePath(null).build().toString());

            File logo = new File(this.getClass().getResource(LOGO).getPath());
            AttachmentDto attachmentDto = newDto(AttachmentDto.class)
                    .withContent(Base64.getEncoder().encodeToString(Files.toByteArray(logo)))
                    .withContentId(LOGO_CID)
                    .withFileName("logo.png");

            EmailBeanDto emailBeanDto = newDto(EmailBeanDto.class)
                    .withBody(Deserializer.resolveVariables(readAndCloseQuietly(getResource(MAIL_TEMPLATE)), props))
                    .withFrom(mailSender)
                    .withTo(mail)
                    .withReplyTo(null)
                    .withSubject(recoverMailSubject)
                    .withMimeType(TEXT_HTML)
                    .withAttachments(Collections.singletonList(attachmentDto));

            mailService.sendMail(emailBeanDto);

        } catch (NotFoundException e) {
            throw new NotFoundException("User " + mail + " is not registered in the system.");
        } catch (ApiException | IOException e) {
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
        String userName = recoveryStorage.get(uuid);

        try {
            final User user = userDao.getByAlias(userName);
            user.setPassword(newPassword);
            userDao.update(user);

            final Profile profile = userProfileDao.getById(user.getId());
            if (profile.getAttributes().remove("resetPassword") != null) {
                userProfileDao.update(profile);
            }
        } catch (NotFoundException e) {
            // remove invalid validationData
            throw new NotFoundException("User " + userName + " is not registered in the system.");
        } catch (ServerException e) {
            LOG.error("Error during setting user's password", e);
            throw new ServerException("Unable to setup password. Please contact support.");
        } finally {
            // remove validation data from validationStorage
            recoveryStorage.remove(uuid);
        }
    }
}
