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

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.codenvy.api.invite.event.InviteCreatedEvent;
import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.shared.invite.dto.InviteDto;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.ProfileManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.mail.EmailBean;
import org.eclipse.che.mail.MailSender;
import org.eclipse.che.mail.template.Template;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link EmailInviteSender}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class EmailInviteSenderTest {
  private static final String API_ENDPOINT = "{HOST}/api";
  private static final String MAIL_FROM = "from@test.com";
  private static final String WORKSPACE_INVITE_SUBJECT = "Welcome to Workspace";
  private static final String ORGANIZATION_INVITE_SUBJECT = "Welcome to Organization";
  private static final String USER_INITIATOR_ID = "userInitiator123";

  @Mock private MailSender mailSender;
  @Mock private UserManager userManager;
  @Mock private ProfileManager profileManager;
  @Mock private BearerTokenAuthenticationHandler tokenHandler;
  @Mock private TemplateProcessor templateProcessor;
  @Mock private EventService eventService;

  @Mock private User initiator;
  @Mock private Profile initiatorProfile;
  @Mock private DefaultEmailResourceResolver resourceResolver;

  private EmailInviteSender emailSender;

  @BeforeMethod
  public void setUp() throws Exception {
    emailSender =
        spy(
            new EmailInviteSender(
                API_ENDPOINT,
                MAIL_FROM,
                WORKSPACE_INVITE_SUBJECT,
                ORGANIZATION_INVITE_SUBJECT,
                resourceResolver,
                mailSender,
                userManager,
                profileManager,
                tokenHandler,
                templateProcessor));

    when(userManager.getById(anyString())).thenReturn(initiator);
    when(profileManager.getById(anyString())).thenReturn(initiatorProfile);
  }

  @Test
  public void shouldSubscribeItself() throws Exception {
    emailSender.subscribe(eventService);

    verify(eventService).subscribe(emailSender, InviteCreatedEvent.class);
  }

  @Test
  public void shouldReturnInitiatorEmailIfFirstNameIsAbsent() throws Exception {
    when(initiator.getEmail()).thenReturn("inititator@test.com");
    when(initiatorProfile.getAttributes()).thenReturn(ImmutableMap.of("lastName", "Last"));

    String initiator = emailSender.getInitiatorInfo(USER_INITIATOR_ID);

    assertEquals(initiator, "inititator@test.com");
    verify(userManager).getById(USER_INITIATOR_ID);
    verify(profileManager).getById(USER_INITIATOR_ID);
  }

  @Test
  public void shouldReturnInitiatorEmailIfLastNameIsAbsent() throws Exception {
    when(initiator.getEmail()).thenReturn("inititator@test.com");
    when(initiatorProfile.getAttributes()).thenReturn(ImmutableMap.of("firstName", "First"));

    String initiator = emailSender.getInitiatorInfo(USER_INITIATOR_ID);

    assertEquals(initiator, "inititator@test.com");
    verify(userManager).getById(USER_INITIATOR_ID);
    verify(profileManager).getById(USER_INITIATOR_ID);
  }

  @Test
  public void shouldReturnFirstNamePlusLastNameIfTheyAreSpecified() throws Exception {
    when(initiatorProfile.getAttributes())
        .thenReturn(
            ImmutableMap.of(
                "firstName", "First",
                "lastName", "Last"));

    String initiator = emailSender.getInitiatorInfo(USER_INITIATOR_ID);

    assertEquals(initiator, "First Last");
    verify(userManager).getById(USER_INITIATOR_ID);
    verify(profileManager).getById(USER_INITIATOR_ID);
  }

  @Test
  public void shouldNotSendEmailInviteWhenInitiatorIdIsNull() throws Exception {
    doNothing().when(emailSender).sendEmail(anyString(), any());

    emailSender.onEvent(
        new InviteCreatedEvent(
            null,
            DtoFactory.newDto(InviteDto.class)
                .withDomainId("test")
                .withInstanceId("instance123")
                .withEmail("user@test.com")));

    verify(emailSender, never()).sendEmail(anyString(), any());
  }

  @Test
  public void shouldNotSendEmailInviteWhenInitiatorIdIsNotNull() throws Exception {
    doNothing().when(emailSender).sendEmail(anyString(), any());
    InviteDto invite =
        DtoFactory.newDto(InviteDto.class)
            .withDomainId("test")
            .withInstanceId("instance123")
            .withEmail("user@test.com");

    emailSender.onEvent(new InviteCreatedEvent(USER_INITIATOR_ID, invite));

    verify(emailSender).sendEmail(USER_INITIATOR_ID, invite);
  }

  @Test(dataProvider = "invitations")
  public void shouldSendEmailInvite(String domain, String subject, Class<Template> templateClass)
      throws Exception {
    when(emailSender.getInitiatorInfo("userok")).thenReturn("INITIATOR");
    when(tokenHandler.generateBearerToken(anyString(), any())).thenReturn("token123");
    when(resourceResolver.resolve(any())).thenAnswer(answer -> answer.getArguments()[0]);
    when(templateProcessor.process(any())).thenReturn("invitation");

    emailSender.sendEmail(
        USER_INITIATOR_ID,
        DtoFactory.newDto(InviteDto.class)
            .withDomainId(domain)
            .withInstanceId("instance123")
            .withEmail("user@test.com"));

    ArgumentCaptor<Template> templateCaptor = ArgumentCaptor.forClass(Template.class);

    verify(emailSender).getInitiatorInfo(USER_INITIATOR_ID);
    verify(templateProcessor).process(templateCaptor.capture());
    Template template = templateCaptor.getValue();
    assertEquals(template.getClass(), templateClass);
    assertEquals(template.getAttributes().get("initiator"), "INITIATOR");
    assertEquals(
        template.getAttributes().get("joinLink"), "{HOST}/site/auth/create?bearertoken=token123");
    ArgumentCaptor<EmailBean> emailBeanCaptor = ArgumentCaptor.forClass(EmailBean.class);
    verify(mailSender).sendAsync(emailBeanCaptor.capture());
    EmailBean sentEmail = emailBeanCaptor.getValue();
    assertEquals(sentEmail.getFrom(), MAIL_FROM);
    assertEquals(sentEmail.getReplyTo(), MAIL_FROM);
    assertEquals(sentEmail.getBody(), "invitation");
    assertEquals(sentEmail.getSubject(), subject);
    assertEquals(sentEmail.getTo(), "user@test.com");
    assertEquals(sentEmail.getMimeType(), TEXT_HTML);
    verify(tokenHandler).generateBearerToken("user@test.com", Collections.emptyMap());
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldThrowServerExceptionWhenSpecifiedUnsupportedDomain() throws Exception {
    emailSender.sendEmail(
        USER_INITIATOR_ID,
        DtoFactory.newDto(InviteDto.class)
            .withDomainId("unsupported")
            .withInstanceId("instance123")
            .withEmail("user@test.com"));
  }

  @DataProvider(name = "invitations")
  public Object[][] getInvitations() {
    return new Object[][] {
      {OrganizationDomain.DOMAIN_ID, ORGANIZATION_INVITE_SUBJECT, MemberInvitationTemplate.class},
      {WorkspaceDomain.DOMAIN_ID, WORKSPACE_INVITE_SUBJECT, WorkerInvitationTemplate.class}
    };
  }
}
