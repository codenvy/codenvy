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

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Provide extension point to change functionality depends on token state.
 *
 * @author Sergii Kabashniuk
 */
public interface TokenHandler {

  /**
   * Handle situation with valid token.
   *
   * @param request - http request.
   * @param response - http response.
   * @param chain - filter chain.
   * @param session - http session associated with given token.
   * @param principal - user associated with given token.
   * @throws IOException
   * @throws ServletException
   */
  void handleValidToken(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      HttpSession session,
      SsoClientPrincipal principal)
      throws IOException, ServletException;

  /**
   * Handle situation when token existed but it's not valid.
   *
   * @param request - http request.
   * @param response - http response.
   * @param chain - filter chain.
   * @param token - invalid token.
   * @throws IOException
   * @throws ServletException
   */
  void handleBadToken(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain, String token)
      throws IOException, ServletException;

  /**
   * Handle situation when token is not exist.
   *
   * @param request - http request.
   * @param response - http response.
   * @param chain - filter chain.
   * @throws IOException
   * @throws ServletException
   */
  void handleMissingToken(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException;
}
