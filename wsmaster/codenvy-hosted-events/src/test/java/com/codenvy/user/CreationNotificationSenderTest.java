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
package com.codenvy.user;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.service.password.RecoveryStorage;
import org.eclipse.che.mail.EmailBean;
import org.eclipse.che.mail.MailSender;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link CreationNotificationSender}
 *
 * @author Sergii Leschenko
 * @author Anton Korneta
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CreationNotificationSenderTest {
  @Captor private ArgumentCaptor<EmailBean> argumentCaptor;

  @Mock private DefaultEmailResourceResolver resourceResolver;
  @Mock private TemplateProcessor templateProcessor;
  @Mock private MailSender mailSender;

  private CreationNotificationSender notificationSender;

  @BeforeMethod
  public void setUp() {
    RecoveryStorage recoveryStorage = mock(RecoveryStorage.class);
    when(recoveryStorage.generateRecoverToken(anyString())).thenReturn("uuid");
    notificationSender =
        new CreationNotificationSender(
            "http://localhost/api",
            "noreply@host",
            recoveryStorage,
            mailSender,
            templateProcessor,
            resourceResolver,
            "Subject without password",
            "Subject with password");
  }

  @Test
  public void shouldSendEmailWhenUserWasCreatedByUserServiceWithDescriptor() throws Throwable {
    when(templateProcessor.process(any())).thenReturn("body");
    when(resourceResolver.resolve(any())).thenAnswer(answer -> answer.getArguments()[0]);
    notificationSender.sendNotification("user123", "test@user.com", true);

    verify(mailSender).sendMail(argumentCaptor.capture());
    verify(resourceResolver, times(1)).resolve(any(EmailBean.class));
    verify(templateProcessor, times(1)).process(any());

    final EmailBean emailBean = argumentCaptor.getValue();
    assertTrue(!emailBean.getBody().isEmpty());
    assertEquals(emailBean.getTo(), "test@user.com");
    assertEquals(emailBean.getMimeType(), TEXT_HTML);
    assertEquals(emailBean.getFrom(), "noreply@host");
    assertEquals(emailBean.getSubject(), "Subject with password");
  }
}
