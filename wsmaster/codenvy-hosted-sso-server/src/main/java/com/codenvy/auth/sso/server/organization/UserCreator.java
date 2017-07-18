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
package com.codenvy.auth.sso.server.organization;


import org.eclipse.che.api.core.model.user.User;

import java.io.IOException;

/**
 * Temporary bridge between authentication and organization.
 * In future have to be replaced with direct HTTP calls.
 *
 * @author Sergii Kabashniuk
 */
public interface UserCreator {

    /**
     * Create new persistant user.
     *
     * @param email
     * @param userName
     * @param firstName
     * @param lastName
     * @throws IOException
     */
    User createUser(String email, String userName, String firstName, String lastName) throws IOException;

    /**
     * Create temporary user.
     *
     * @return - name of temporary user.
     * @throws IOException
     */
    User createTemporary() throws IOException;
}
