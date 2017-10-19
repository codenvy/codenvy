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
package com.codenvy.api.dao.authentication;
/** @author Sergii Kabashniuk */

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Holder of AuthenticationHandlers.
 *
 * @author Sergii Kabashniuk
 */
public class AuthenticationHandlerProvider {

  private final Map<String, AuthenticationHandler> handlers;
  private final AuthenticationHandler defaultHandler;

  @Inject
  public AuthenticationHandlerProvider(
      Set<AuthenticationHandler> handlers,
      @Named("sys.auth.handler.default") String defaultHandler) {
    this.handlers = new HashMap<>(handlers.size());
    for (AuthenticationHandler handler : handlers) {
      this.handlers.put(handler.getType(), handler);
    }
    this.defaultHandler = this.handlers.get(defaultHandler);
    if (this.defaultHandler == null) {
      throw new IllegalArgumentException(
          "AuthenticationHandler with type "
              + defaultHandler
              + " is not found. And can't be default.");
    }
  }

  /**
   * Search handler by type.
   *
   * @param handlerType - given handler type.
   * @return - AuthenticationHandler with given type.
   */
  public AuthenticationHandler getHandler(String handlerType) {
    return handlers.get(handlerType);
  }

  /** @return AuthenticationHandler which handle request without type. */
  public AuthenticationHandler getDefaultHandler() {
    return defaultHandler;
  }
}
