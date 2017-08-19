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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.codenvy.auth.sso.server.EmailValidator;
import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.security.oauth.OAuthAuthenticationException;
import org.eclipse.che.security.oauth.OAuthAuthenticator;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OAuthLoginServlet extends HttpServlet {
  private static final Logger LOG = LoggerFactory.getLogger(OAuthLoginServlet.class);
  @Inject private UserManager userManager;
  @Inject private OAuthAuthenticatorProvider authenticatorProvider;
  @Inject private BearerTokenAuthenticationHandler handler;
  @Inject private EmailValidator emailValidator;

  @Named("auth.sso.create_workspace_page_url")
  @Inject
  private String createWorkspacePage;

  @Named("auth.no.account.found.page")
  @Inject
  private String noAccountFoundErrorPage;

  @Inject
  @Named("che.auth.user_self_creation")
  private boolean userSelfCreationAllowed;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String email = req.getParameter("email");
    String oauthProvider = req.getParameter("oauth_provider");

    String bearertoken = req.getParameter("oauthbearertoken");

    if (email == null || oauthProvider == null || bearertoken == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    try {
      handler.authenticate(bearertoken);
    } catch (AuthenticationException e) {
      resp.sendError(e.getResponseStatus(), e.getLocalizedMessage());
      return;
    }

    if (email.contains("+")) {
      req.setAttribute("errorMessage", "Email with '+' is not allowed for registration");
      req.getRequestDispatcher("/login.html").forward(req, resp);
      return;
    }

    OAuthAuthenticator authenticator = authenticatorProvider.getAuthenticator(oauthProvider);
    if (authenticator == null) {
      LOG.error("Unknown OAuthAuthenticatorProvider  {} ", oauthProvider);
      resp.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "Authentication failed. Unknown oauth_provider " + oauthProvider);
      return;
    }

    final OAuthToken token = authenticator.getToken(email);
    if (token == null || isNullOrEmpty(token.getToken())) {
      LOG.error("Unable obtain email address for user {} ", email);
      resp.sendError(
          HttpServletResponse.SC_BAD_REQUEST, "Unable obtain email address for user " + email);
      return;
    }

    Optional<User> userOptional = getUserByEmail(email);
    if (userOptional.isPresent()) {
      // send user on login
      URI uri =
          UriBuilder.fromUri(createWorkspacePage)
              .replaceQuery(req.getQueryString())
              .replaceQueryParam("signature")
              .replaceQueryParam("oauth_provider")
              .replaceQueryParam("oauthbearertoken")
              .queryParam(
                  "bearertoken",
                  handler.generateBearerToken(
                      email,
                      userOptional.get().getName(),
                      Collections.singletonMap("initiator", oauthProvider)))
              .build();

      LOG.debug("Oauth login. Redirect after: {}", uri.toString());
      resp.sendRedirect(uri.toString());
    } else {
      if (!userSelfCreationAllowed) {
        resp.sendRedirect(noAccountFoundErrorPage);
        return;
      }

      // fill user profile if user doesn't exists and login in first time.
      Map<String, String> profileInfo = createProfileInfo(email, authenticator, token);
      profileInfo.put("initiator", oauthProvider);

      try {
        emailValidator.validateUserMail(email);

        URI uri =
            UriBuilder.fromUri(createWorkspacePage)
                .replaceQuery(req.getQueryString())
                .replaceQueryParam("signature")
                .replaceQueryParam("oauth_provider")
                .replaceQueryParam("bearertoken", handler.generateBearerToken(email, profileInfo))
                .build();

        resp.sendRedirect(uri.toString());
      } catch (ServerException | BadRequestException e) {
        throw new ServletException(e.getLocalizedMessage(), e);
      }
    }
  }

  private Optional<User> getUserByEmail(String email) throws IOException {
    try {
      User user = userManager.getByEmail(email);
      return Optional.of(user);
    } catch (NotFoundException e) {
      return Optional.empty();
    } catch (ServerException e) {
      throw new IOException(e.getLocalizedMessage(), e);
    }
  }

  Map<String, String> createProfileInfo(
      String email, OAuthAuthenticator authenticator, OAuthToken token) {
    Map<String, String> profileInfo = new HashMap<>();
    try {
      try {
        userManager.getByEmail(email);
      } catch (NotFoundException e) {
        String fullName = authenticator.getUser(token).getName();
        if (fullName != null && !fullName.isEmpty()) {
          String firstName, lastName = "";
          String[] names = fullName.trim().split(" ", 2);
          firstName = names[0].trim();
          if (names.length > 1) lastName = names[1].trim();
          if (!(firstName.isEmpty() && lastName.isEmpty())) {
            if (!firstName.isEmpty()) {
              profileInfo.put("firstName", firstName);
            }
            if (!lastName.isEmpty()) {
              profileInfo.put("lastName", lastName);
            }
          }
        }
      }
    } catch (ApiException | OAuthAuthenticationException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    return profileInfo;
  }
}
