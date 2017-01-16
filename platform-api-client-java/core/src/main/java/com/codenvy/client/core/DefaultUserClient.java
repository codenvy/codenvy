/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.core;

import com.codenvy.client.Request;
import com.codenvy.client.UserClient;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.core.model.DefaultUser;
import com.codenvy.client.model.User;

import javax.ws.rs.client.Invocation;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * The Codenvy user API client.
 *
 * @author Kevin Pollet
 * @author Stéphane Daviet
 */
public class DefaultUserClient extends AbstractClient implements UserClient {
    /**
     * Constructs an instance of {@link DefaultUserClient}.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param authenticationManager
     *         the {@link AuthenticationManager}.
     * @throws NullPointerException
     *         if url or authenticationManager parameter is {@code null}.
     */
    DefaultUserClient(String url, AuthenticationManager authenticationManager) {
        super(url, "user", authenticationManager);
    }

    /**
     * Returns the current user.
     *
     * @return the current user.
     */
    public Request<User> current() {
        final Invocation request = getWebTarget().request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();

        return new SimpleRequest<User>(request, DefaultUser.class, getAuthenticationManager());
    }
}
