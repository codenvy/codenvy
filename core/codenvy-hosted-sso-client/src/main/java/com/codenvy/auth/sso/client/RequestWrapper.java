/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
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
            public boolean isUserInRole(String role) {
                return subject.isMemberOf(role);
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
