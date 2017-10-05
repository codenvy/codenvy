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
package com.codenvy.auth.sso.oauth;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.codenvy.auth.sso.server.EmailValidator;
import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.security.oauth.OAuthAuthenticationException;
import org.eclipse.che.security.oauth.OAuthAuthenticator;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProvider;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class CreateUserWithCapturedProfileInfoTest {
  private static final String USERNAME = "user@gmail.com";
  private static final OAuthToken TOKEN =
      DtoFactory.getInstance().createDto(OAuthToken.class).withToken("1231243");
  @Mock private ServletContext servletContext;
  @Mock private OAuthAuthenticator authenticator;
  @Mock private UserManager userManager;
  @Mock private ServletConfig servletConfig;
  @Mock private OAuthAuthenticatorProvider authenticatorProvider;
  @Mock private BearerTokenAuthenticationHandler handler;
  @Mock private org.eclipse.che.security.oauth.shared.User googleUser;
  @Mock private EmailValidator emailValidator;

  @InjectMocks private OAuthLoginServlet oAuthLoginServlet;

  @BeforeMethod
  public void setUp() throws Exception {
    // oAuthLoginServlet = new OAuthLoginServlet();
    when(servletConfig.getServletContext()).thenReturn(servletContext);

    //oAuthLoginServlet.init(servletConfig);
  }

  @Test
  public void shouldParseFirstAndLastNames() throws OAuthAuthenticationException, ApiException {
    when(googleUser.getName()).thenReturn("Mark Downey");
    when(userManager.getByEmail(USERNAME)).thenThrow(NotFoundException.class);
    when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

    Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

    assertEquals(res.get("firstName"), "Mark");
    assertEquals(res.get("lastName"), "Downey");
  }

  @Test
  public void shouldParseFirstNames()
      throws OAuthAuthenticationException, ServletException, ApiException {

    when(googleUser.getName()).thenReturn("Mark");
    when(userManager.getByEmail(USERNAME)).thenThrow(NotFoundException.class);
    when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

    Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

    assertEquals(res.get("firstName"), "Mark");
    assertNull(res.get("lastName"));
  }

  @Test
  public void shouldParseFirstNamesAndTrim()
      throws OAuthAuthenticationException, ServletException, ApiException {
    when(googleUser.getName()).thenReturn("  Mark   ");

    when(userManager.getByEmail(USERNAME)).thenThrow(NotFoundException.class);
    when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

    Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

    assertEquals(res.get("firstName"), "Mark");
    assertNull(res.get("lastName"));
  }

  @Test
  public void shouldIgnoreOneSpaceName()
      throws OAuthAuthenticationException, ServletException, ApiException {
    when(googleUser.getName()).thenReturn(" ");

    when(userManager.getByEmail(USERNAME)).thenThrow(NotFoundException.class);
    when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

    Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

    assertEquals(res.size(), 0);
  }

  @Test
  public void shouldTrimSpaces()
      throws OAuthAuthenticationException, ServletException, ApiException {
    when(googleUser.getName()).thenReturn("   Mark    Downey    ");

    when(userManager.getByEmail(USERNAME)).thenThrow(NotFoundException.class);
    when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

    Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

    assertEquals(res.get("firstName"), "Mark");
    assertEquals(res.get("lastName"), "Downey");
  }

  @Test
  public void shouldParseFirstAndLastNamesWithFewWords()
      throws OAuthAuthenticationException, ServletException, ApiException {

    when(googleUser.getName()).thenReturn("Mark Tyler Downey Jewel");

    when(userManager.getByEmail(USERNAME)).thenThrow(NotFoundException.class);
    when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

    Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

    assertEquals(res.get("firstName"), "Mark");
    assertEquals(res.get("lastName"), "Tyler Downey Jewel");
  }
}
