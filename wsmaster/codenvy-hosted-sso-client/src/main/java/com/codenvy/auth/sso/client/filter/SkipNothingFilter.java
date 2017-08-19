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
package com.codenvy.auth.sso.client.filter;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of RequestFilter that tels LoginFilter to handle all request.
 *
 * @author Sergii Kabashniuk
 */
public class SkipNothingFilter implements RequestFilter {
  @Override
  public boolean shouldSkip(HttpServletRequest request) {
    return false;
  }
}
