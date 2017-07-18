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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Invalidate sso client session associated with given token.
 *
 * @author Sergii Kabashniuk
 * @author Andrey Parfonov
 */
@Singleton
public class SSOLogoutServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(SSOLogoutServlet.class);
    @Inject
    protected SessionStore sessionStore;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String token = req.getParameter("authToken");
        if (token == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Token is not set");
            return;
        }
        HttpSession session = sessionStore.removeSessionByToken(token);
        if (session != null) {
            session.removeAttribute("principal");
            session.invalidate();
            LOG.debug("SSO logout [token: {}, session: {}, context {}]", token, session.getId(),
                      session.getServletContext().getServletContextName());
        } else {
            LOG.warn("Not found session associated to {}", token);
        }
    }
}
