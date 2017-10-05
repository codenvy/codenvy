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
package com.codenvy.api.invite.email;

import static com.google.api.client.repackaged.com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

import com.codenvy.api.invite.event.InviteCreatedEvent;
import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.shared.invite.model.Invite;
import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.user.server.ProfileManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.mail.EmailBean;
import org.eclipse.che.mail.MailSender;
import org.eclipse.che.mail.template.Template;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.eclipse.che.mail.template.exception.TemplateException;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends email invite to user after initial invite storing.
 *
 * @author Sergii Leschenko
 */
public class EmailInviteSender implements EventSubscriber<InviteCreatedEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(EmailInviteSender.class);

  private final String apiEndpoint;
  private final String mailFrom;
  private final String workspaceInviteSubject;
  private final String organizationInviteSubject;
  private final MailSender mailSender;
  private final UserManager userManager;
  private final ProfileManager profileManager;
  private final BearerTokenAuthenticationHandler tokenHandler;
  private final TemplateProcessor templateProcessor;
  private final DefaultEmailResourceResolver resourceResolver;

  @Inject
  public EmailInviteSender(
      @Named("che.api") String apiEndpoint,
      @Named("che.mail.from_email_address") String mailFrom,
      @Named("workspace.email.invite.subject") String workspaceInviteSubject,
      @Named("organization.email.invite.subject") String organizationInviteSubject,
      DefaultEmailResourceResolver resourceResolver,
      MailSender mailSender,
      UserManager userManager,
      ProfileManager profileManager,
      BearerTokenAuthenticationHandler tokenHandler,
      TemplateProcessor templateProcessor) {
    this.apiEndpoint = apiEndpoint;
    this.mailFrom = mailFrom;
    this.workspaceInviteSubject = workspaceInviteSubject;
    this.organizationInviteSubject = organizationInviteSubject;
    this.mailSender = mailSender;
    this.userManager = userManager;
    this.profileManager = profileManager;
    this.tokenHandler = tokenHandler;
    this.templateProcessor = templateProcessor;
    this.resourceResolver = resourceResolver;
  }

  @Inject
  public void subscribe(EventService eventService) {
    eventService.subscribe(this, InviteCreatedEvent.class);
  }

  @Override
  public void onEvent(InviteCreatedEvent event) {
    if (event.getInitiatorId() != null) {
      Invite invite = event.getInvite();
      try {
        sendEmail(event.getInitiatorId(), invite);
      } catch (ServerException e) {
        LOG.warn(
            "Error while processing email invite to {} with id {} for user. Cause: {}",
            invite.getDomainId(),
            invite.getInstanceId(),
            invite.getEmail(),
            e.getLocalizedMessage());
      }
    }
  }

  /**
   * Sends email notifications about user invitation.
   *
   * <p>Note that it sends email in async way, so there is no guarantee that notification will be
   * transported.
   *
   * @param initiatorId user's id who send invitation
   * @param invite invite to sending email notification
   * @throws ServerException when there is no configured template for specified domain
   * @throws ServerException when any other exception occurs during email processing
   */
  public void sendEmail(String initiatorId, Invite invite) throws ServerException {
    String email = invite.getEmail();
    String bearerToken = tokenHandler.generateBearerToken(email, new HashMap<>());
    String joinLink =
        apiEndpoint.replace("/api", "") + "/site/auth/create?bearertoken=" + bearerToken;
    String initiator = getInitiatorInfo(initiatorId);

    Template template;
    String subject;
    switch (invite.getDomainId()) {
      case OrganizationDomain.DOMAIN_ID:
        template = new MemberInvitationTemplate(initiator, joinLink);
        subject = organizationInviteSubject;
        break;
      case WorkspaceDomain.DOMAIN_ID:
        template = new WorkerInvitationTemplate(initiator, joinLink);
        subject = workspaceInviteSubject;
        break;
      default:
        throw new ServerException(
            format(
                "There is no configured template for specified %s domain", invite.getDomainId()));
    }
    try {
      mailSender.sendAsync(
          resourceResolver.resolve(
              new EmailBean()
                  .withSubject(subject)
                  .withBody(templateProcessor.process(template))
                  .withFrom(mailFrom)
                  .withReplyTo(mailFrom)
                  .withTo(email)
                  .withMimeType(TEXT_HTML)));
    } catch (TemplateException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @VisibleForTesting
  String getInitiatorInfo(String initiatorId) throws ServerException {
    User initiator;
    Profile initiatorProfile;
    try {
      initiator = userManager.getById(initiatorId);
      initiatorProfile = profileManager.getById(initiatorId);
    } catch (NotFoundException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }

    Map<String, String> profileAttributes = initiatorProfile.getAttributes();
    String firstName = nullToEmpty(profileAttributes.get("firstName"));
    String lastName = nullToEmpty(profileAttributes.get("lastName"));

    if (firstName.isEmpty() || lastName.isEmpty()) {
      return initiator.getEmail();
    } else {
      return firstName + " " + lastName;
    }
  }
}
