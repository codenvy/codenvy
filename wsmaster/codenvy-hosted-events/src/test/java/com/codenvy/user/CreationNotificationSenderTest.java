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
package com.codenvy.user;

import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.mail.EmailBean;
import com.codenvy.mail.MailSender;
import com.codenvy.service.password.RecoveryStorage;
import com.codenvy.template.processor.html.HTMLTemplateProcessor;
import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link CreationNotificationSender}
 *
 * @author Sergii Leschenko
 * @author Anton Korneta
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CreationNotificationSenderTest {
    @Captor
    private ArgumentCaptor<EmailBean> argumentCaptor;

    @Mock
    private DefaultEmailResourceResolver             resourceResolver;
    @Mock
    private HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf;
    @Mock
    private MailSender                               mailSender;

    private CreationNotificationSender notificationSender;

    @BeforeMethod
    public void setUp() {
        RecoveryStorage recoveryStorage = mock(RecoveryStorage.class);
        when(recoveryStorage.generateRecoverToken(anyString())).thenReturn("uuid");
        notificationSender = new CreationNotificationSender("http://localhost/api",
                                                            "noreply@host",
                                                            recoveryStorage,
                                                            mailSender,
                                                            thymeleaf,
                                                            resourceResolver);
    }

    @Test
    public void shouldSendEmailWhenUserWasCreatedByUserServiceWithDescriptor() throws Throwable {
        when(thymeleaf.process(any())).thenReturn("body");
        when(resourceResolver.resolve(any())).thenAnswer(answer -> answer.getArguments()[0]);
        notificationSender.sendNotification("user123", "test@user.com", true);

        verify(mailSender).sendMail(argumentCaptor.capture());
        verify(resourceResolver, times(1)).resolve(any(EmailBean.class));
        verify(thymeleaf, times(1)).process(any());

        final EmailBean emailBean = argumentCaptor.getValue();
        assertTrue(!emailBean.getBody().isEmpty());
        assertEquals(emailBean.getTo(), "test@user.com");
        assertEquals(emailBean.getMimeType(), TEXT_HTML);
        assertEquals(emailBean.getFrom(), "noreply@host");
        assertEquals(emailBean.getSubject(), "Welcome To Codenvy");
    }

}
