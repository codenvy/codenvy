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
import com.codenvy.client.VersionClient;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.core.model.DefaultVersion;
import com.codenvy.client.model.Version;

import javax.ws.rs.client.Invocation;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * The Codenvy version API client.
 *
 * @author Florent Benoit
 */
public class DefaultVersionClient extends AbstractClient implements VersionClient {

    /**
     * Constructs an instance of {@link com.codenvy.client.core.DefaultVersionClient}.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param authenticationManager
     *         the {@link com.codenvy.client.core.auth.AuthenticationManager}.
     * @throws NullPointerException
     *         if url or authenticationManager parameter is {@code null}.
     */
    DefaultVersionClient(String url, AuthenticationManager authenticationManager) {
        super(url, "", authenticationManager);
    }

    /**
     * Returns the current user.
     *
     * @return the current user.
     */
    public Request<Version> current() {
        final Invocation request = getWebTarget().request()
                                                 .accept(APPLICATION_JSON)
                                                 .build("OPTIONS");

        return new SimpleRequest<Version>(request, DefaultVersion.class, getAuthenticationManager());
    }
}
