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

import org.eclipse.che.api.auth.AuthenticationException;

/**
 * Authentication using username and password.
 *
 * @author Sergii Kabashniuk
 */
public interface AuthenticationHandler {
    /**
     * Check user password.
     *
     * @return User id  of authenticated user.
     * @throws AuthenticationException
     *         - in case if login name and password are not matched.
     */
    String authenticate(final String login, final String password) throws AuthenticationException;

    /** @return - type of authentication handler */
    String getType();
}
