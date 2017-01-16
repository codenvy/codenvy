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
package com.codenvy.auth.sso.client;

import org.everrest.test.mock.MockHttpServletRequest;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@Listeners(value = MockitoTestNGListener.class)
public class SSOLogoutServletTest {

    @Mock
    HttpServletResponse response;
    @Mock
    SessionStore        sessionStore;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    HttpSession session;

    @InjectMocks
    SSOLogoutServlet servlet;

    @Test
    public void shouldFailIfTokenIsNotSet() throws ServletException, IOException {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/sso/logout", null, 0, "POST", null);
        //when
        servlet.doPost(request, response);
        //when                                    ,
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), eq("Token is not set"));

    }

    @Test
    public void shouldRemoveToken() throws ServletException, IOException {
        //given
        MockHttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/sso/logout", null, 0, "POST", null);
        request.setParameter("authToken", "t-12312344");
        //when
        servlet.doPost(request, response);
        //when
        verify(sessionStore).removeSessionByToken("t-12312344");
        verifyZeroInteractions(response);

    }

    @Test
    public void shouldCleanupHttpSession() throws ServletException, IOException {
        //given
        MockHttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/sso/logout", null, 0, "POST", null);
        request.setParameter("authToken", "t-12312344");
        when(sessionStore.removeSessionByToken(eq("t-12312344"))).thenReturn(session);
        //when
        servlet.doPost(request, response);
        //when
        verify(session).invalidate();
        verify(session).removeAttribute(eq("principal"));

    }
}
