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
/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
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
package com.codenvy.service.password;

import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.mail.EmailBean;
import com.codenvy.mail.MailSender;
import com.codenvy.template.processor.html.HTMLTemplateProcessor;
import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.user.server.ProfileManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.tools.DependencySupplierImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;

import static com.jayway.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** Test of features from PasswordService */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class PasswordServiceTest {
    private static final String SERVICE_PATH = "/password";
    private static final String NEW_PASSWORD = "new password";
    private static final String UUID         = java.util.UUID.randomUUID().toString();
    private static final String USER_EMAIL   = "user@mail.com";

    @Mock
    private MailSender mailSender;

    @Mock
    private UserManager userManager;
    @Mock
    private ProfileManager profileManager;
    @Mock
    private RecoveryStorage recoveryStorage;
    @Mock
    private ProfileImpl profile;
    @Mock
    private DefaultEmailResourceResolver resourceResolver;
    @Mock
    private HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf;

    private User user;

    private PasswordService passService;

    @SuppressWarnings("unused")
    ApiExceptionMapper exceptionMapper;

    @BeforeMethod
    public void setup() throws Exception {
        passService = new PasswordService(mailSender,
                                          userManager,
                                          recoveryStorage,
                                          profileManager,
                                          resourceResolver,
                                          "Codenvy <noreply@codenvy.com>",
                                          "Codenvy Password Recovery",
                                          thymeleaf,
                                          1);
        user = spy(new UserImpl(UUID, USER_EMAIL, USER_EMAIL));

        doReturn(USER_EMAIL).when(user).getEmail();
        DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addInstance(UserManager.class, userManager);
        dependencies.addInstance(ProfileManager.class, profileManager);

    }

    @Test
    public void shouldBeAbleToSetupPass() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(true);
        when(recoveryStorage.get(UUID)).thenReturn(USER_EMAIL);
        when(userManager.getByEmail(USER_EMAIL)).thenReturn(user);
        doReturn("userId").when(user).getId();
        when(profileManager.getById(eq("userId"))).thenReturn(profile);
        when(profile.getAttributes()).thenReturn(Collections.<String, String>emptyMap());

        Response response =  given().formParam("uuid", UUID)
                                    .formParam("password", NEW_PASSWORD)
                                    .when()
                                    .post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 204);

        verify(recoveryStorage).remove(UUID);
        verify(userManager).update(any(User.class));
        verify(profileManager, never()).update(profile);
    }

    @Test
    public void shouldBeAbleToSetupPassAndRemoveResetPassFlagFromProfile() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(true);
        when(recoveryStorage.get(UUID)).thenReturn(USER_EMAIL);
        when(userManager.getByEmail(USER_EMAIL)).thenReturn(user);
        doReturn("userId").when(user).getId();
        when(profileManager.getById(eq("userId"))).thenReturn(profile);
        when(profile.getAttributes()).thenReturn(new HashMap<String, String>() {{
            put("resetPassword", "true");
        }});

        Response response =
                given().formParam("uuid", UUID).formParam("password", NEW_PASSWORD).when()
                       .post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 204);

        verify(recoveryStorage).remove(UUID);
        verify(userManager).update(any(User.class));
        verify(profileManager).update(profile);
    }

    @Test
    public void shouldRespond403OnSetupPassForInvalidRecord() {
        when(recoveryStorage.isValid(UUID)).thenReturn(false);

        Response response =
                given().formParam("uuid", UUID).formParam("password", "newpass").when().post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 403);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(),
                     "Setup password token is incorrect or has expired");

        verify(recoveryStorage, times(1)).remove(UUID);
        verifyZeroInteractions(userManager);
    }

    @Test
    public void shouldRespond404OnSetupPassForNonRegisteredUser() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(true);
        when(recoveryStorage.get(UUID)).thenReturn(USER_EMAIL);
        doThrow(new NotFoundException(USER_EMAIL)).when(userManager).getByEmail(USER_EMAIL);

        Response response =
                given().formParam("uuid", UUID).formParam("password", NEW_PASSWORD).when()
                       .post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 404);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(),
                     "User " + USER_EMAIL + " is not registered in the system.");
        verify(recoveryStorage, times(1)).remove(UUID);
        verify(userManager, never()).update(eq(user));
    }

    @Test
    public void shouldRespond500IfOtherErrorOccursOnSetupPass() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(true);
        when(recoveryStorage.get(UUID)).thenReturn(USER_EMAIL);
        when(userManager.getByEmail(USER_EMAIL)).thenReturn(user);
        doThrow(new ServerException("test")).when(userManager).update(any(User.class));

        Response response =
                given().formParam("uuid", UUID).formParam("password", NEW_PASSWORD).when()
                       .post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 500);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(),
                     "Unable to setup password. Please contact support.");

        verify(recoveryStorage, times(1)).remove(UUID);
        verify(userManager).update(any(User.class));
    }

    @Test
    public void shouldBeAbleToSetupConfirmation() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(true);

        Response response = given().pathParam("uuid", UUID).when().get(SERVICE_PATH + "/verify/{uuid}");

        assertEquals(response.statusCode(), 204);
        verify(recoveryStorage).isValid(eq(UUID));
    }

    @Test
    public void shouldRespond403OnSetupConfirmationInvalidUuid() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(false);

        Response response = given().pathParam("uuid", UUID).when().get(SERVICE_PATH + "/verify/{uuid}");

        assertEquals(response.statusCode(), 403);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(),
                     "Setup password token is incorrect or has expired");
        verify(recoveryStorage).remove(UUID);
    }

    @Test
    public void shouldBeAbleToRecoverPassword(ITestContext ctx) throws Exception {
        when(userManager.getByEmail(USER_EMAIL)).thenReturn(user);
        when(recoveryStorage.generateRecoverToken(eq(USER_EMAIL))).thenReturn(UUID);
        Response response = given().pathParam("username", USER_EMAIL).when().post(SERVICE_PATH + "/recover/{username}");

        assertEquals(response.statusCode(), 204);

        verify(mailSender).sendMail(any(EmailBean.class));
    }

    @Test
    public void shouldSendEmailToRecoverPassword() throws Exception {
        when(userManager.getByEmail(USER_EMAIL)).thenReturn(user);
        when(recoveryStorage.generateRecoverToken(eq(USER_EMAIL))).thenReturn(UUID);
        when(resourceResolver.resolve(any())).thenAnswer(answer -> answer.getArguments()[0]);
        when(thymeleaf.process(any())).thenReturn("body");
        ArgumentCaptor<EmailBean> argumentCaptor = ArgumentCaptor.forClass(EmailBean.class);

        given().pathParam("username", USER_EMAIL).post(SERVICE_PATH + "/recover/{username}");

        verify(mailSender).sendMail(argumentCaptor.capture());
        verify(resourceResolver, times(1)).resolve(any());
        verify(thymeleaf, times(1)).process(any());
        EmailBean argumentCaptorValue = argumentCaptor.getValue();
        assertEquals(argumentCaptorValue.getFrom(), "Codenvy <noreply@codenvy.com>");
        assertEquals(argumentCaptorValue.getSubject(), "Codenvy Password Recovery");
        assertEquals(argumentCaptorValue.getMimeType(), TEXT_HTML);
        assertTrue(!argumentCaptorValue.getBody().isEmpty());
    }

    @Test
    public void shouldSetResponseStatus404IfUserIsNotRegistered() throws Exception {
        doThrow(NotFoundException.class).when(userManager).getByEmail(eq(USER_EMAIL));

        Response response = given().pathParam("username", USER_EMAIL).when().post(SERVICE_PATH + "/recover/{username}");

        assertEquals(response.statusCode(), 404);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(),
                     "User " + USER_EMAIL + " is not registered in the system.");
        verifyZeroInteractions(mailSender);
        verifyZeroInteractions(recoveryStorage);
    }

    @Test
    public void shouldRespond500IfProblemOnEmailSendingOccurs() throws Exception {
        doThrow(new ServerException("error")).when(mailSender).sendMail(any(EmailBean.class));

        Response response = given().pathParam("username", USER_EMAIL).when().post(SERVICE_PATH + "/recover/{username}");

        assertEquals(response.statusCode(), 500);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(),
                     "Unable to recover password. Please contact support or try later.");
    }

    private static <T> T unwrapDto(com.jayway.restassured.response.Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }
}
