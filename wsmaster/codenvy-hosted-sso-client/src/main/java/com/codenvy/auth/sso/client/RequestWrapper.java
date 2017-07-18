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

import org.eclipse.che.commons.subject.Subject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.security.Principal;

/**
 * Wraps HttpServletRequest and provide correct answers for
 * getRemoteUser, isUserInRole and getUserPrincipal getSession.
 */
public class RequestWrapper {

    public HttpServletRequest wrapRequest(final HttpSession session, final HttpServletRequest httpReq,
                                          final Subject subject) {
        return new HttpServletRequestWrapper(httpReq) {
            private final HttpSession httpSession = session;

            @Override

            public String getRemoteUser() {
                return subject.getUserName();
            }

            @Override
            public Principal getUserPrincipal() {
                return new Principal() {
                    @Override
                    public String getName() {
                        return subject.getUserName();
                    }
                };
            }

            @Override
            public HttpSession getSession() {
                return httpSession;
            }
        };
    }
}
