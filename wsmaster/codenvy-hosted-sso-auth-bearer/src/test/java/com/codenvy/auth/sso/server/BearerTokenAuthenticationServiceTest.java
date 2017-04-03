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
package com.codenvy.auth.sso.server;

import com.codenvy.api.dao.authentication.CookieBuilder;
import com.codenvy.api.dao.authentication.TicketManager;
import com.codenvy.api.dao.authentication.TokenGenerator;
import com.codenvy.api.license.server.SystemLicenseManager;
import com.codenvy.api.license.shared.model.Constants;
import com.codenvy.auth.sso.server.BearerTokenAuthenticationService.ValidationData;
import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import com.codenvy.auth.sso.server.organization.UserCreationValidator;
import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.mail.EmailBean;
import com.codenvy.mail.MailSender;
import com.codenvy.template.processor.html.HTMLTemplateProcessor;
import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.user.server.UserValidator;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.jayway.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
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
    private BearerTokenAuthenticationHandler         handler;
    @Mock
    private MailSender                               mailSender;
    @Mock
    private EmailValidator                           emailValidator;
    @Mock
    private CookieBuilder                            cookieBuilder;
    @Mock
    private UserCreationValidator                    creationValidator;
    @Mock
    private UserCreator                              userCreator;
    @Mock
    private SystemLicenseManager                     licenseManager;
    @Mock
    private DefaultEmailResourceResolver             resourceResolver;
    @Mock
    private HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf;

    private BearerTokenAuthenticationService bearerTokenAuthenticationService;

    @SuppressWarnings("unused")
    private ApiExceptionMapper apiExceptionMapper;

    @BeforeMethod
    public void setUp() throws Exception {
        bearerTokenAuthenticationService = new BearerTokenAuthenticationService(mock(TicketManager.class),
                                                                                mock(TokenGenerator.class),
                                                                                handler,
                                                                                mailSender,
                                                                                emailValidator,
                                                                                cookieBuilder,
                                                                                creationValidator,
                                                                                userCreator,
                                                                                mock(UserValidator.class),
                                                                                licenseManager,
                                                                                resourceResolver,
                                                                                thymeleaf,
                                                                                "noreply@host");
    }

    @Test
    public void shouldSendEmailToValidateUserEmailAndUserName() throws Exception {
        ArgumentCaptor<EmailBean> argumentCaptor = ArgumentCaptor.forClass(EmailBean.class);
        ValidationData validationData = new ValidationData("Email", "UserName");
        when(licenseManager.isFairSourceLicenseAccepted()).thenReturn(true);
        when(licenseManager.canUserBeAdded()).thenReturn(true);
        when(resourceResolver.resolve(any())).thenAnswer(answer -> answer.getArguments()[0]);
        when(thymeleaf.process(any())).thenReturn("email body");

        given().contentType(ContentType.JSON).content(validationData).post("/internal/token/validate");

        verify(mailSender).sendMail(argumentCaptor.capture());
        verify(thymeleaf, times(1)).process(any());
        verify(resourceResolver, times(1)).resolve(any());
        EmailBean argumentCaptorValue = argumentCaptor.getValue();
        assertTrue(!argumentCaptorValue.getBody().isEmpty());
        assertEquals(argumentCaptorValue.getMimeType(), TEXT_HTML);
        assertEquals(argumentCaptorValue.getFrom(), "noreply@host");
        assertEquals(argumentCaptorValue.getSubject(), "Verify Your Codenvy Account");
    }

    @Test
    public void shouldThrowAnExceptionWhenUserBeyondTheLicense() throws Exception {
        ValidationData validationData = new ValidationData("Email", "UserName");
        when(licenseManager.isFairSourceLicenseAccepted()).thenReturn(true);
        when(licenseManager.canUserBeAdded()).thenReturn(false);

        Response response =
                given().contentType(ContentType.JSON).content(validationData).post("/internal/token/validate");

        assertEquals(response.getStatusCode(), 403);
        assertEquals(DtoFactory.getInstance().createDtoFromJson(response.asString(), ServiceError.class),
                     newDto(ServiceError.class).withMessage(Constants.UNABLE_TO_ADD_ACCOUNT_BECAUSE_OF_LICENSE));
        verifyZeroInteractions(mailSender);
    }

    @Test
    public void shouldThrowAnExceptionWhenFairSourceLicenseIsNotAccepted() throws Exception {
        ValidationData validationData = new ValidationData("Email", "UserName");
        when(licenseManager.isFairSourceLicenseAccepted()).thenReturn(false);

        Response response =
                given().contentType(ContentType.JSON).content(validationData).post("/internal/token/validate");

        assertEquals(response.getStatusCode(), 403);
        assertEquals(DtoFactory.getInstance().createDtoFromJson(response.asString(), ServiceError.class),
                     newDto(ServiceError.class).withMessage(Constants.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE));
        verifyZeroInteractions(mailSender);
    }
}
