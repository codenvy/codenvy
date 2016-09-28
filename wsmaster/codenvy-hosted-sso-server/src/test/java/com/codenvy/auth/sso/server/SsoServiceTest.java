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

import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.dao.authentication.CookieBuilder;
import com.codenvy.api.dao.authentication.TicketManager;
import com.codenvy.auth.sso.shared.dto.SubjectDto;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.auth.AuthenticationExceptionMapper;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Sergii Kabashniuk
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class SsoServiceTest {
    AuthenticationExceptionMapper exceptionMapper = new AuthenticationExceptionMapper();
    @Mock
    TicketManager              ticketManager;
    @Mock
    SecureRandomTokenGenerator uniqueTokenGenerator;
    @Mock
    CookieBuilder              cookieBuilder;
    @InjectMocks
    SsoService                 ssoService;
    @Mock
    UserManager                userManager;
    User user = new UserImpl("id-1", "emai@host.com", "name");


    @Test
    public void shouldReturn400IfTokenIsInvalid() {
        given()
                .pathParam("token", "t1")
                .queryParam("clienturl", "http://dev.box.com/api")
                .then()
                .expect().statusCode(400)
                .when()
                .get("internal/sso/server/{token}");
    }


    @Test
    public void shouldReturn400IfClientUrlIsNotGiven() {
        given()
                .pathParam("token", "t1")
                .then()
                .expect().statusCode(400)
                .when()
                .get("internal/sso/server/{token}");
    }

    @Test
    public void shouldValidUserIfTokenIsValid() throws NotFoundException, ServerException {
        //given
        when(userManager.getById(eq(user.getId()))).thenReturn(user);
        AccessTicket ticket = new AccessTicket("t1", user.getId(), "default");
        SubjectDto subjectDto = DtoFactory.newDto(SubjectDto.class)
                                          .withName(user.getName())
                                          .withId(user.getId())
                                          .withToken(ticket.getAccessToken());

        when(ticketManager.getAccessTicket(eq("t1"))).thenReturn(ticket);

        //when
        final Response response = given()
                .pathParam("token", "t1")
                .queryParam("clienturl", "http://dev.box.com/api")
                .then()
                .expect().statusCode(200)
                .when()
                .get("internal/sso/server/{token}");
        //then
        assertEquals(unwrapDto(response, SubjectDto.class), subjectDto);
        assertTrue(ticket.getRegisteredClients().contains("http://dev.box.com/api"));
    }

    @Test
    public void shouldUnregisterClientByToken() {
        //given

        //when
        given()
                .pathParam("token", "t1")
                .then()
                .expect().statusCode(204)
                .when()
                .delete("internal/sso/server/{token}");
        //then
        verify(ticketManager).removeTicket("t1");
    }

    @Test
    public void shouldUnregisterClientByTokenAndUrl() {
        //given
        AccessTicket ticket = new AccessTicket("t1", "id-34", "default");
        ticket.registerClientUrl("http://dev.box.com/api");
        when(ticketManager.getAccessTicket(eq("t1"))).thenReturn(ticket);
        //when
        given()
                .pathParam("token", "t1")
                .queryParam("clienturl", "http://dev.box.com/api")
                .then()
                .expect().statusCode(204)
                .when()
                .delete("internal/sso/server/{token}");
        //then
        verify(ticketManager).getAccessTicket(eq("t1"));
        verifyNoMoreInteractions(ticketManager);
        assertEquals(ticket.getRegisteredClients().size(), 0);
    }

    private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }
}
