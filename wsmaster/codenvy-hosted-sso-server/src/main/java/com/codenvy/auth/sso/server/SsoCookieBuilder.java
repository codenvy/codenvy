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
package com.codenvy.auth.sso.server;


import com.codenvy.api.dao.authentication.CookieBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * Utility class to helps build response after authentication.
 * <p/>
 * It allow to to set or remove such cookies:
 * <p/>
 * 1) token-access-key   - persistent cooke visible from  accessCookiePath path
 * 2) session-access-key - session cooke visible from  "/" path
 * 3) logged_in          - persistent cooke. Indicated that nonanonymous user is logged in.
 *
 * @author Sergii Kabashniuk
 * @author Alexander Garagatyi
 */
public class SsoCookieBuilder implements CookieBuilder {
    @Named("auth.sso.access_cookie_path")
    @Inject
    private String accessCookiePath;
    @Named("auth.sso.access_ticket_lifetime_seconds")
    @Inject
    private int    ticketLifeTimeSeconds;

    public void clearCookies(Response.ResponseBuilder builder, String token, boolean secure) {
        if (token != null && !token.isEmpty()) {
            builder.header("Set-Cookie", new NewCookie("token-access-key", token, accessCookiePath, null, null, 0, secure) + ";HttpOnly");
            builder.header("Set-Cookie", new NewCookie("session-access-key", token, "/", null, null, 0, secure) + ";HttpOnly");
        }
        builder.cookie(new NewCookie("logged_in", "true", "/", null, null, 0, secure));
    }

    @Override
    public void setCookies(Response.ResponseBuilder builder, String token, boolean secure) {
        setCookies(builder, token, secure, false);
    }

    public void setCookies(Response.ResponseBuilder builder, String token, boolean secure, boolean isAnonymous) {
        builder.header("Set-Cookie",
                       new NewCookie("token-access-key", token, accessCookiePath, null, null, ticketLifeTimeSeconds, secure) + ";HttpOnly");
        builder.header("Set-Cookie", new NewCookie("session-access-key", token, "/", null, null, -1, secure) + ";HttpOnly");
        if (!isAnonymous) {
            builder.cookie(
                    new NewCookie("logged_in", "true", "/", null, null, ticketLifeTimeSeconds, secure));
        }
    }
}