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
package com.codenvy.api.dao.authentication;


import com.jayway.restassured.http.ContentType;

import org.eclipse.che.api.auth.AuthenticationExceptionMapper;
import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Collections;

import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Test for AuthenticationService class.
 *
 * @author Sergii Kabashniuk
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class AuthenticationServiceTest {
    @Mock
    protected AuthenticationHandler         handler;
    @Mock
    protected AuthenticationHandlerProvider handlerProvider;
    @Mock
    protected Subject                       principal;
    @Mock
    protected Subject                       oldPrincipal;
    @Mock
    protected TicketManager                 ticketManager;
    @Mock
    protected CookieBuilder                 cookieBuilder;
    @Mock
    protected TokenGenerator                uniqueTokenGenerator;
    @InjectMocks
    protected AuthenticationDaoImpl         dao;

    protected AuthenticationService service;

    @SuppressWarnings("unused")
    protected AuthenticationService authenticationService;
    protected String                token;
    protected String                tokenOld;

    @SuppressWarnings("unused")
    private ExceptionMapper exceptionMapper = new AuthenticationExceptionMapper();

    @BeforeMethod
    public void init() {
        token = "t1";
        tokenOld = "t2";
        when(ticketManager.getAccessTicket(eq(tokenOld))).thenReturn(new AccessTicket(tokenOld, oldPrincipal, "default"));
        when(handlerProvider.getDefaultHandler()).thenReturn(handler);
        when(handlerProvider.getHandler(anyString())).thenReturn(handler);
        when(handler.getType()).thenReturn("default");
        when(uniqueTokenGenerator.generate()).thenReturn(token);
        service = new AuthenticationService(dao);
    }

    @Test
    public void shouldAuthenticateWithCorrectParams() throws Exception {
        //given
        when(handler.authenticate(eq("user@site.com"), eq("secret")))
                .thenReturn(new SubjectImpl("user@site.com", "14433", "t11", Collections.<String>emptyList(), false));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Response.ResponseBuilder builder = (Response.ResponseBuilder)args[0];
                String token = (String)args[1];
                boolean secure = (boolean)args[2];
                if (token != null && !token.isEmpty()) {
                    builder.header("Set-Cookie",
                                   new NewCookie("token-access-key", token, "/sso/server", null, null, 0, secure) + ";HttpOnly");
                    builder.header("Set-Cookie", new NewCookie("session-access-key", token, "/", null, null, 0, secure) + ";HttpOnly");
                }
                builder.cookie(new NewCookie("logged_in", "true", "/", null, null, 0, secure));
                return null;
            }
        }).when(cookieBuilder).setCookies(any(Response.ResponseBuilder.class), anyString(), anyBoolean());

        // when
        final Token receivedToken = unwrapDto(given()
                                                      .contentType(ContentType.JSON)
                                                      .body(DtoFactory.getInstance().createDto(Credentials.class)
                                                                      .withUsername("user@site.com")
                                                                      .withPassword("secret"))
                                                      .then()
                                                      .expect().statusCode(200)
                                                      .cookie("token-access-key", token)
                                                      .cookie("session-access-key", token)
                                                      .cookie("logged_in", "true")

                                                      .when()
                                                      .post("/auth/login"),
                                              Token.class);

        ArgumentCaptor<AccessTicket> argument = ArgumentCaptor.forClass(AccessTicket.class);
        verify(ticketManager).putAccessTicket(argument.capture());
        assertEquals(receivedToken.getValue(), this.token);
        assertEquals(argument.getValue().getAccessToken(), this.token);
    }


    @Test
    public void shouldReturnBadRequestIfLoginIsNotSet() throws Exception {
        //given
        // when
        given().contentType(ContentType.JSON)
               .body(DtoFactory.getInstance().createDto(Credentials.class)
                               .withUsername(null)
                               .withPassword("secret"))
               .then()
               .expect().statusCode(400)
               .when()
               .post("/auth/login");
    }

    @Test
    public void shouldReturnBadRequestIfLoginIsEmpty() throws Exception {
        //given
        // when
        given().contentType(ContentType.JSON)
               .body(DtoFactory.getInstance().createDto(Credentials.class)
                               .withUsername("")
                               .withPassword("secret"))
               .then()
               .expect().statusCode(400)
               .when()
               .post("/auth/login");
    }

    @Test
    public void shouldReturnBadRequestIfPassowordIsNotSet() throws Exception {
        //given
        // when
        given().contentType(ContentType.JSON)
               .body(DtoFactory.getInstance().createDto(Credentials.class)
                               .withUsername("user@site.com")
                               .withPassword(null))
               .then()
               .expect().statusCode(400)
               .when()
               .post("/auth/login");
    }

    @Test
    public void shouldReturnBadRequestIfPasswordIsEmpty() throws Exception {
        //given
        // when
        given().contentType(ContentType.JSON)
               .body(DtoFactory.getInstance().createDto(Credentials.class)
                               .withUsername("user@site.com")
                               .withPassword(""))
               .then()
               .expect().statusCode(400)
               .when()
               .post("/auth/login");
    }

    @Test
    public void shouldReturnBadRequestIfHandlerNotAbleToAuthenticate() throws Exception {
        //given
        // when
        System.out.println(given().contentType(ContentType.JSON)
                                  .body(DtoFactory.getInstance().createDto(Credentials.class)
                                                  .withUsername("user@site.com")
                                                  .withPassword("asdfasdf"))
                                   .then()
//               .expect().statusCode(400)
                                   .when()
                                   .post("/auth/login").print());
    }

    @Test
    public void shouldLogoutFirstIfUserAlreadyLoggedIn() throws Exception {
        //given
        when(handler.authenticate(eq("user@site.com"), eq("secret")))
                .thenReturn(new SubjectImpl("user@site.com", "14433", "t111", Collections.<String>emptyList(), false));
        when(oldPrincipal.getUserName()).thenReturn("old@site.com");
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Response.ResponseBuilder builder = (Response.ResponseBuilder)args[0];
                String token = (String)args[1];
                boolean secure = (boolean)args[2];
                if (token != null && !token.isEmpty()) {
                    builder.header("Set-Cookie",
                                   new NewCookie("token-access-key", token, "/sso/server", null, null, 0, secure) + ";HttpOnly");
                    builder.header("Set-Cookie", new NewCookie("session-access-key", token, "/", null, null, 0, secure) + ";HttpOnly");
                }
                builder.cookie(new NewCookie("logged_in", "true", "/", null, null, 0, secure));
                return null;
            }
        }).when(cookieBuilder).setCookies(any(Response.ResponseBuilder.class), anyString(), anyBoolean());

        // when
        final Token receivedToken = unwrapDto(given().contentType(ContentType.JSON)
                                                     .cookie("session-access-key", tokenOld)
                                                     .body(DtoFactory.getInstance().createDto(Credentials.class)
                                                                     .withUsername("user@site.com")
                                                                     .withPassword("secret"))
                                                     .then()
                                                     .expect().statusCode(200)
                                                     .cookie("token-access-key", this.token)
                                                     .cookie("session-access-key", this.token)
                                                     .cookie("logged_in", "true")

                                                     .when()
                                                     .post("/auth/login"),
                                              Token.class);

        ArgumentCaptor<AccessTicket> argument = ArgumentCaptor.forClass(AccessTicket.class);
        verify(ticketManager).removeTicket(eq(tokenOld));
        verify(ticketManager).putAccessTicket(argument.capture());
        assertEquals(receivedToken.getValue(), this.token);
        assertEquals(argument.getValue().getAccessToken(), this.token);
    }

    @Test
    public void shouldBeAbleToLogoutByQueryParameter() throws Exception {
        //given
        when(ticketManager.removeTicket(eq(tokenOld))).thenReturn(new AccessTicket(tokenOld, oldPrincipal, "default"));
        // when
        given()
                .contentType(ContentType.JSON)
                .queryParam("token", tokenOld)
                .then()
                .expect().statusCode(200)
                .when()
                .post("/auth/logout");
        //then
        verify(ticketManager).removeTicket(eq(tokenOld));
    }

    @Test
    public void shouldBeAbleToLogoutByCookie() throws Exception {
        //given
        when(ticketManager.removeTicket(eq(tokenOld))).thenReturn(new AccessTicket(tokenOld, oldPrincipal, "default"));
        // when
        given()
                .contentType(ContentType.JSON)
                .cookie("session-access-key", tokenOld)
                .then()
                .expect().statusCode(200)
                .when()
                .post("/auth/logout");
        //then
        verify(ticketManager).removeTicket(eq(tokenOld));
    }

    @Test
    public void shouldFailToLogoutWithoutToken() throws Exception {
        //given
        // when
        given()
                .contentType(ContentType.JSON)
                .then()
                .expect().statusCode(400)
                .when()
                .post("/auth/logout");

    }

    private static <T> T unwrapDto(com.jayway.restassured.response.Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }
}
