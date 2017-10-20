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
package com.codenvy.auth.sso.client;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.everrest.test.mock.MockHttpServletRequest;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = MockitoTestNGListener.class)
public class SSOLogoutServletTest {

  @Mock HttpServletResponse response;
  @Mock SessionStore sessionStore;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  HttpSession session;

  @InjectMocks SSOLogoutServlet servlet;

  @Test
  public void shouldFailIfTokenIsNotSet() throws ServletException, IOException {
    // given
    HttpServletRequest request =
        new MockHttpServletRequest("http://localhost:8080/sso/logout", null, 0, "POST", null);
    // when
    servlet.doPost(request, response);
    // when                                    ,
    verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), eq("Token is not set"));
  }

  @Test
  public void shouldRemoveToken() throws ServletException, IOException {
    // given
    MockHttpServletRequest request =
        new MockHttpServletRequest("http://localhost:8080/sso/logout", null, 0, "POST", null);
    request.setParameter("authToken", "t-12312344");
    // when
    servlet.doPost(request, response);
    // when
    verify(sessionStore).removeSessionByToken("t-12312344");
    verifyZeroInteractions(response);
  }

  @Test
  public void shouldCleanupHttpSession() throws ServletException, IOException {
    // given
    MockHttpServletRequest request =
        new MockHttpServletRequest("http://localhost:8080/sso/logout", null, 0, "POST", null);
    request.setParameter("authToken", "t-12312344");
    when(sessionStore.removeSessionByToken(eq("t-12312344"))).thenReturn(session);
    // when
    servlet.doPost(request, response);
    // when
    verify(session).invalidate();
    verify(session).removeAttribute(eq("principal"));
  }
}
