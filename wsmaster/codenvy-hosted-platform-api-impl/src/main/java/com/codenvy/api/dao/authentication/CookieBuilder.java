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
package com.codenvy.api.dao.authentication;

import javax.ws.rs.core.Response;

/**
 * Utility class to helps build response after authentication.
 * <p/>
 * It allow to to set or remove such cookies:
 * <p/>
 */
public interface CookieBuilder {

    public void clearCookies(Response.ResponseBuilder builder, String token, boolean secure);

    public void setCookies(Response.ResponseBuilder builder, String token, boolean secure);
}
