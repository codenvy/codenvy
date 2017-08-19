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
package com.codenvy.auth.sso.server;

import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.model.impl.UserImpl;

/** Token validator implementation. */
public class BearerTokenValidator implements TokenValidator {

  @Inject private BearerTokenAuthenticationHandler handler;

  @Override
  public User validateToken(String token) throws ConflictException {
    Map<String, String> payload = handler.getPayload(token);
    String username = handler.getPayload(token).get("username");
    if (username == null || !handler.isValid(token))
      throw new ConflictException(
          "Cannot create user - authentication token is invalid. Request a new one.");
    return new UserImpl(null, payload.get("email"), payload.get("username"));
  }
}
