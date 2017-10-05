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
package com.codenvy.auth.sso.server;

import static com.jayway.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.api.dao.authentication.CookieBuilder;
import com.codenvy.api.dao.authentication.TicketManager;
import com.codenvy.api.dao.authentication.TokenGenerator;
import com.codenvy.auth.sso.server.BearerTokenAuthenticationService.ValidationData;
import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import com.codenvy.auth.sso.server.organization.UserCreationValidator;
import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.mail.DefaultEmailResourceResolver;
import com.jayway.restassured.http.ContentType;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.user.server.UserValidator;
import org.eclipse.che.mail.EmailBean;
import org.eclipse.che.mail.MailSender;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.everrest.assured.EverrestJetty;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link BearerTokenAuthenticationService}
 *
 * @author Igor Vinokur
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class BearerTokenAuthenticationServiceTest {

  @Mock private BearerTokenAuthenticationHandler handler;
  @Mock private MailSender mailSender;
  @Mock private EmailValidator emailValidator;
  @Mock private CookieBuilder cookieBuilder;
  @Mock private UserCreationValidator creationValidator;
  @Mock private UserCreator userCreator;
  @Mock private DefaultEmailResourceResolver resourceResolver;
  @Mock private TemplateProcessor templateProcessor;

  private BearerTokenAuthenticationService bearerTokenAuthenticationService;

  @SuppressWarnings("unused")
  private ApiExceptionMapper apiExceptionMapper;

  @BeforeMethod
  public void setUp() throws Exception {
    bearerTokenAuthenticationService =
        new BearerTokenAuthenticationService(
            mock(TicketManager.class),
            mock(TokenGenerator.class),
            handler,
            mailSender,
            emailValidator,
            cookieBuilder,
            creationValidator,
            userCreator,
            mock(UserValidator.class),
            resourceResolver,
            templateProcessor,
            "noreply@host",
            "Subject");
  }

  @Test
  public void shouldSendEmailToValidateUserEmailAndUserName() throws Exception {
    ArgumentCaptor<EmailBean> argumentCaptor = ArgumentCaptor.forClass(EmailBean.class);
    ValidationData validationData = new ValidationData("Email", "UserName");
    when(resourceResolver.resolve(any())).thenAnswer(answer -> answer.getArguments()[0]);
    when(templateProcessor.process(any())).thenReturn("email body");
    when(handler.generateBearerToken(anyString(), anyString(), any())).thenReturn("token");

    given().contentType(ContentType.JSON).content(validationData).post("/internal/token/validate");

    verify(mailSender).sendMail(argumentCaptor.capture());
    verify(templateProcessor, times(1)).process(any());
    verify(resourceResolver, times(1)).resolve(any());
    EmailBean argumentCaptorValue = argumentCaptor.getValue();
    assertTrue(!argumentCaptorValue.getBody().isEmpty());
    assertEquals(argumentCaptorValue.getMimeType(), TEXT_HTML);
    assertEquals(argumentCaptorValue.getFrom(), "noreply@host");
    assertEquals(argumentCaptorValue.getSubject(), "Subject");
  }
}
