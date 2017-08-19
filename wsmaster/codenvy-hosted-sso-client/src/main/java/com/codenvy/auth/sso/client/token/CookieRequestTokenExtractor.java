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
package com.codenvy.auth.sso.client.token;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/** Extract sso token from cookies. */
public class CookieRequestTokenExtractor implements RequestTokenExtractor {
  public static final String SECRET_TOKEN_ACCESS_COOKIE = "session-access-key";

  @Override
  public String getToken(HttpServletRequest req) {
    Cookie[] cookies = req.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (SECRET_TOKEN_ACCESS_COOKIE.equalsIgnoreCase(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
