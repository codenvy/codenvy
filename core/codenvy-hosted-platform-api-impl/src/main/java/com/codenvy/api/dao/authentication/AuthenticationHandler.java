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
package com.codenvy.api.dao.authentication;

import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.commons.subject.Subject;

/**
 * Authentication using username and password.
 *
 * @author Sergii Kabashniuk
 */
public interface AuthenticationHandler {
    /**
     * Authenticate user.
     *
     * @return - user principal if authentication is done, throw an {@link com.codenvy.api.auth.AuthenticationException}
     * otherwise.
     * @throws AuthenticationException
     */
    Subject authenticate(final String login, final String password) throws AuthenticationException;

    /** @return - type of authentication handler */
    String getType();
}
