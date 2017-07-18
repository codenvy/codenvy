/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.auth.sso.client;

import com.google.inject.ImplementedBy;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;

/**
 * Construct path to SSO client from request url.
 */
@ImplementedBy(WebAppClientUrlExtractor.class)
public interface ClientUrlExtractor {
    String getClientUrl(HttpServletRequest req) throws MalformedURLException;
}
