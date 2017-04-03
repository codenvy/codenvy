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
package com.codenvy.api.invite.email;

import com.codenvy.api.invite.event.InviteCreatedEvent;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.mail.EmailBean;
import com.codenvy.mail.MailSender;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.shared.invite.dto.InviteDto;
import com.codenvy.template.processor.html.HTMLTemplateProcessor;
import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;
import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.ProfileManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Test for {@link EmailInviteSender}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class EmailInviteSenderTest {
    private static final String API_ENDPOINT                = "{HOST}/api";
    private static final String MAIL_FROM                   = "from@test.com";
    private static final String WORKSPACE_INVITE_SUBJECT    = "Welcome to Workspace";
    private static final String ORGANIZATION_INVITE_SUBJECT = "Welcome to Organization";
    private static final String USER_INITIATOR_ID           = "userInitiator123";

    @Mock
    private MailSender                               mailSender;
    @Mock
    private UserManager                              userManager;
    @Mock
    private ProfileManager                           profileManager;
    @Mock
    private BearerTokenAuthenticationHandler         tokenHandler;
    @Mock
    private HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf;
    @Mock
    private EventService                             eventService;

    @Mock
    private User                         initiator;
    @Mock
    private Profile                      initiatorProfile;
    @Mock
    private DefaultEmailResourceResolver resourceResolver;

    private EmailInviteSender emailSender;

    @BeforeMethod
    public void setUp() throws Exception {
        emailSender = spy(new EmailInviteSender(API_ENDPOINT,
                                                MAIL_FROM,
                                                WORKSPACE_INVITE_SUBJECT,
                                                ORGANIZATION_INVITE_SUBJECT,
                                                resourceResolver,
                                                mailSender,
                                                userManager,
                                                profileManager,
                                                tokenHandler,
                                                thymeleaf));

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
        when(initiatorProfile.getAttributes()).thenReturn(ImmutableMap.of("firstName", "First",
                                                                          "lastName", "Last"));

        String initiator = emailSender.getInitiatorInfo(USER_INITIATOR_ID);

        assertEquals(initiator, "First Last");
        verify(userManager).getById(USER_INITIATOR_ID);
        verify(profileManager).getById(USER_INITIATOR_ID);
    }

    @Test
    public void shouldNotSendEmailInviteWhenInitiatorIdIsNull() throws Exception {
        doNothing().when(emailSender).sendEmail(anyString(), any());

        emailSender.onEvent(new InviteCreatedEvent(null,
                                                   DtoFactory.newDto(InviteDto.class)
                                                             .withDomainId("test")
                                                             .withInstanceId("instance123")
                                                             .withEmail("user@test.com")));

        verify(emailSender, never()).sendEmail(anyString(), any());
    }

    @Test
    public void shouldNotSendEmailInviteWhenInitiatorIdIsNotNull() throws Exception {
        doNothing().when(emailSender).sendEmail(anyString(), any());
        InviteDto invite = DtoFactory.newDto(InviteDto.class)
                                     .withDomainId("test")
                                     .withInstanceId("instance123")
                                     .withEmail("user@test.com");

        emailSender.onEvent(new InviteCreatedEvent(USER_INITIATOR_ID, invite));

        verify(emailSender).sendEmail(USER_INITIATOR_ID, invite);
    }

    @Test(dataProvider = "invitations")
    public void shouldSendEmailInvite(String domain, String subject, Class<? extends ThymeleafTemplate> templateClass)
            throws Exception {
        when(emailSender.getInitiatorInfo("userok")).thenReturn("INITIATOR");
        when(tokenHandler.generateBearerToken(anyString(), any())).thenReturn("token123");
        when(resourceResolver.resolve(any())).thenAnswer(answer -> answer.getArguments()[0]);
        when(thymeleaf.process(any())).thenReturn("invitation");

        emailSender.sendEmail(USER_INITIATOR_ID, DtoFactory.newDto(InviteDto.class)
                                                           .withDomainId(domain)
                                                           .withInstanceId("instance123")
                                                           .withEmail("user@test.com"));

        ArgumentCaptor<ThymeleafTemplate> templateCaptor = ArgumentCaptor.forClass(ThymeleafTemplate.class);

        verify(emailSender).getInitiatorInfo(USER_INITIATOR_ID);
        verify(thymeleaf).process(templateCaptor.capture());
        ThymeleafTemplate template = templateCaptor.getValue();
        assertEquals(template.getClass(), templateClass);
        assertEquals(template.getContext().getVariable("initiator"), "INITIATOR");
        assertEquals(template.getContext().getVariable("joinLink"), "{HOST}/site/auth/create?bearertoken=token123");
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
        emailSender.sendEmail(USER_INITIATOR_ID,
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
