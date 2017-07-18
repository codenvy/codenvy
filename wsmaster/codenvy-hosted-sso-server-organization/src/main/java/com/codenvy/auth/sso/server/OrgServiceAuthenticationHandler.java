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

import com.codenvy.api.dao.authentication.AuthenticationHandler;

import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Authentication using username and password based on UserDao
 *
 * @author Sergii Kabashniuk
 */
public class OrgServiceAuthenticationHandler implements AuthenticationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(OrgServiceAuthenticationHandler.class);

    @Inject
    UserDao userDao;

    public String authenticate(final String login, final String password)
            throws AuthenticationException {
        if (login == null || login.isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException(401, "Authentication failed. Please check username and password.");
        }

        try {
           return userDao.getByAliasAndPassword(login, password).getId();
        } catch (ApiException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            throw new AuthenticationException(401, "Authentication failed. Please check username and password.");
        }
    }

    @Override
    public String getType() {
        return "org";
    }

}
