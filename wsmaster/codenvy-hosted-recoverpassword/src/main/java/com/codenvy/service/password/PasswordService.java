/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.service.password;

import static javax.ws.rs.core.MediaType.TEXT_HTML;

import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.service.password.email.template.PasswordRecoveryTemplate;
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
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.user.server.ProfileManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.mail.EmailBean;
import org.eclipse.che.mail.MailSender;
import org.eclipse.che.mail.SendMailException;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.eclipse.che.mail.template.exception.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Services for password features
 *
 * @author Michail Kuznyetsov
 */
@Path("/password")
public class PasswordService {

  private static final Logger LOG = LoggerFactory.getLogger(PasswordService.class);

  private final MailSender mailService;
  private final UserManager userManager;
  private final ProfileManager profileManager;
  private final RecoveryStorage recoveryStorage;
  private final DefaultEmailResourceResolver resourceResolver;
  private final String mailFrom;
  private final String recoverPasswordMailSubject;
  private final TemplateProcessor templateProcessor;
  private final long validationMaxAge;

  @Context private UriInfo uriInfo;

  @Inject
  public PasswordService(
      MailSender mailSender,
      UserManager userManager,
      RecoveryStorage recoveryStorage,
      ProfileManager profileManager,
      DefaultEmailResourceResolver resourceResolver,
      @Named("che.mail.from_email_address") String mailFrom,
      @Named("account.password.recovery.mail.subject") String recoverPasswordMailSubject,
      TemplateProcessor templateProcessor,
      @Named("password.recovery.expiration_timeout_hours") long validationMaxAge) {
    this.recoveryStorage = recoveryStorage;
    this.mailService = mailSender;
    this.userManager = userManager;
    this.profileManager = profileManager;
    this.resourceResolver = resourceResolver;
    this.mailFrom = mailFrom;
    this.recoverPasswordMailSubject = recoverPasswordMailSubject;
    this.templateProcessor = templateProcessor;
    this.validationMaxAge = validationMaxAge;
  }

  /**
   * Sends mail for password restoring
   *
   * <p>
   *
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
   * @param mail the identifier of user
   */
  @POST
  @Path("recover/{usermail}")
  public void recoverPassword(@PathParam("usermail") String mail)
      throws ServerException, NotFoundException {
    try {
      //check if user exists
      userManager.getByEmail(mail);
      final String masterEndpoint =
          uriInfo.getBaseUriBuilder().replacePath(null).build().toString();
      final String tokenAgeMessage = String.valueOf(validationMaxAge) + " hour";
      final String uuid = recoveryStorage.generateRecoverToken(mail);

      mailService.sendMail(
          resourceResolver.resolve(
              new EmailBean()
                  .withBody(getEmailBody(masterEndpoint, tokenAgeMessage, uuid))
                  .withFrom(mailFrom)
                  .withTo(mail)
                  .withReplyTo(null)
                  .withSubject(recoverPasswordMailSubject)
                  .withMimeType(TEXT_HTML)));
    } catch (NotFoundException e) {
      throw new NotFoundException("User " + mail + " is not registered in the system.");
    } catch (SendMailException | ApiException e) {
      LOG.error("Error during setting user's password", e);
      throw new ServerException("Unable to recover password. Please contact support or try later.");
    }
  }

  private String getEmailBody(String masterEndpoint, String tokenAgeMessage, String uuid)
      throws ServerException {
    try {
      return templateProcessor.process(
          new PasswordRecoveryTemplate(tokenAgeMessage, masterEndpoint, uuid));
    } catch (TemplateException e) {
      throw new ServerException(e.getMessage());
    }
  }

  /**
   * Verify setup password confirmation token.
   *
   * <p>
   *
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
   * @param uuid token of setup password operation
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
   *
   * <p>
   *
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
   * @param uuid token of setup password operation
   * @param newPassword new users password
   */
  @POST
  @Path("setup")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public void setupPassword(
      @FormParam("uuid") String uuid, @FormParam("password") String newPassword)
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
      final UserImpl user = new UserImpl(userManager.getByEmail(email));
      user.setPassword(newPassword);
      userManager.update(user);

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
