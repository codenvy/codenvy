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
package com.codenvy.api.dao.authentication;


import com.jayway.restassured.http.ContentType;

import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.auth.AuthenticationExceptionMapper;
import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;
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

import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
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
    protected TicketManager                 ticketManager;
    @Mock
    protected CookieBuilder                 cookieBuilder;
    @Mock
    protected TokenGenerator                uniqueTokenGenerator;
    @Mock
    protected UserDao                       userDao;

    private final static UserImpl USER   = new UserImpl("id-123123", "user@site.com", "userSuperMonster");
    private final static UserImpl USER_2 = new UserImpl("id-444", "mini@site.com", "MiniMonster");
    @InjectMocks
    protected AuthenticationDaoImpl dao;


    protected AuthenticationService service;

    protected String token;
    protected String token2;

    @SuppressWarnings("unused")
    private AuthenticationExceptionMapper exceptionMapper = new AuthenticationExceptionMapper();

    @BeforeMethod
    public void init() throws NotFoundException, ServerException {
        token = "t1";
        token2 = "t2";
        when(ticketManager.getAccessTicket(eq(token2))).thenReturn(new AccessTicket(token2, USER_2.getId(), "default"));
        when(handlerProvider.getDefaultHandler()).thenReturn(handler);
        when(handlerProvider.getHandler(anyString())).thenReturn(handler);
        when(handler.getType()).thenReturn("default");
        when(uniqueTokenGenerator.generate()).thenReturn(token);
        when(userDao.getByName(USER.getName())).thenReturn(USER);
        when(userDao.getByName(USER_2.getName())).thenReturn(USER_2);
        service = new AuthenticationService(dao);
    }

    @Test
    public void shouldAuthenticateWithCorrectParams() throws Exception {
        //given

        when(handler.authenticate(eq(USER.getName()), eq("secret"))).thenReturn(USER.getId());
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Response.ResponseBuilder builder = (Response.ResponseBuilder)args[0];
            String token1 = (String)args[1];
            boolean secure = (boolean)args[2];
            if (token1 != null && !token1.isEmpty()) {
                builder.header("Set-Cookie",
                               new NewCookie("token-access-key", token1, "/sso/server", null, null, 0, secure) + ";HttpOnly");
                builder.header("Set-Cookie", new NewCookie("session-access-key", token1, "/", null, null, 0, secure) + ";HttpOnly");
            }
            builder.cookie(new NewCookie("logged_in", "true", "/", null, null, 0, secure));
            return null;
        }).when(cookieBuilder).setCookies(any(Response.ResponseBuilder.class), anyString(), anyBoolean());

        // when
        final Token receivedToken = unwrapDto(given()
                                                      .contentType(ContentType.JSON)
                                                      .body(DtoFactory.getInstance().createDto(Credentials.class)
                                                                      .withUsername(USER.getName())
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
                               .withUsername(USER.getName())
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
                               .withUsername(USER.getName())
                               .withPassword(""))
               .then()
               .expect().statusCode(400)
               .when()
               .post("/auth/login");
    }

    @Test
    public void shouldReturnBadRequestIfHandlerNotAbleToAuthenticate() throws Exception {
        //given
        doThrow(new AuthenticationException("Not able to authenticate")).when(handler).authenticate(eq(USER.getName()), eq("asdfasdf"));
        // when
        given().contentType(ContentType.JSON)
               .body(DtoFactory.getInstance().createDto(Credentials.class)
                               .withUsername(USER.getName())
                               .withPassword("asdfasdf"))
               .then()
               .expect().statusCode(400)
               .when()
               .post("/auth/login");
    }

    @Test
    public void shouldLogoutFirstIfUserAlreadyLoggedIn() throws Exception {
        //given
        when(handler.authenticate(eq(USER.getName()), eq("secret"))).thenReturn(USER.getId());
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
                                                     .cookie("session-access-key", token2)
                                                     .body(DtoFactory.getInstance().createDto(Credentials.class)
                                                                     .withUsername(USER.getName())
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
        verify(ticketManager).removeTicket(eq(token2));
        verify(ticketManager).putAccessTicket(argument.capture());
        assertEquals(receivedToken.getValue(), this.token);
        assertEquals(argument.getValue().getAccessToken(), this.token);
    }

    @Test
    public void shouldBeAbleToLogoutByQueryParameter() throws Exception {
        //given
        when(ticketManager.removeTicket(eq(token2))).thenReturn(new AccessTicket(token2, USER_2.getId(), "default"));
        // when
        given()
                .contentType(ContentType.JSON)
                .queryParam("token", token2)
                .then()
                .expect().statusCode(200)
                .when()
                .post("/auth/logout");
        //then
        verify(ticketManager).removeTicket(eq(token2));
    }

    @Test
    public void shouldBeAbleToLogoutByCookie() throws Exception {
        //given
        when(ticketManager.removeTicket(eq(token2))).thenReturn(new AccessTicket(token2, USER_2.getId(), "default"));
        // when
        given()
                .contentType(ContentType.JSON)
                .cookie("session-access-key", token2)
                .then()
                .expect().statusCode(200)
                .when()
                .post("/auth/logout");
        //then
        verify(ticketManager).removeTicket(eq(token2));
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
