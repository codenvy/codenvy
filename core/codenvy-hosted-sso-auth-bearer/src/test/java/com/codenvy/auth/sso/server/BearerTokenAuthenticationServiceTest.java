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
package com.codenvy.auth.sso.server;

import com.codenvy.api.dao.authentication.CookieBuilder;
import com.codenvy.auth.sso.server.BearerTokenAuthenticationService.ValidationData;
import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import com.codenvy.auth.sso.server.organization.UserCreationValidator;
import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.mail.MailSenderClient;
import com.codenvy.mail.shared.dto.EmailBeanDto;
import com.jayway.restassured.http.ContentType;

import org.everrest.assured.EverrestJetty;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.jayway.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test for {@link BearerTokenAuthenticationService}
 *
 * @author Igor Vinokur
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class BearerTokenAuthenticationServiceTest {

    @Mock
    private BearerTokenAuthenticationHandler handler;
    @Mock
    private MailSenderClient                 mailSenderClient;
    @Mock
    private InputDataValidator               inputDataValidator;
    @Mock
    private CookieBuilder                    cookieBuilder;
    @Mock
    private UserCreationValidator            creationValidator;
    @Mock
    private UserCreator                      userCreator;

    @InjectMocks
    private BearerTokenAuthenticationService bearerTokenAuthenticationService;

    @Test
    public void shouldSendEmailToValidateUserEmailAndUserName() throws Exception {
        bearerTokenAuthenticationService.mailSender = "noreply@host";
        ArgumentCaptor<EmailBeanDto> argumentCaptor = ArgumentCaptor.forClass(EmailBeanDto.class);
        ValidationData validationData = new ValidationData("Email", "UserName");

        given().contentType(ContentType.JSON).content(validationData).post("/internal/token/validate");

        verify(mailSenderClient).sendMail(argumentCaptor.capture());
        EmailBeanDto argumentCaptorValue = argumentCaptor.getValue();
        assertTrue(argumentCaptorValue.getAttachments().size() == 1);
        assertTrue(!argumentCaptorValue.getBody().isEmpty());
        assertEquals(argumentCaptorValue.getMimeType(), TEXT_HTML);
        assertEquals(argumentCaptorValue.getFrom(), "noreply@host");
        assertEquals(argumentCaptorValue.getSubject(), "Verify Your Codenvy Account");
    }
}
