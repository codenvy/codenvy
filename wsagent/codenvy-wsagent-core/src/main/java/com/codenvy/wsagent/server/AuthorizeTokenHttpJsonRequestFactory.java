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
package com.codenvy.wsagent.server;

import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Extends {@link DefaultHttpJsonRequestFactory} and aware about user's authorization token.
 * <p>Used for the purpose of sending authorized requests from WS-agent to the WS-master.
 *
 * @author Artem Zatsarynnyi
 * @see DefaultHttpJsonRequestFactory
 */
@Singleton
public class AuthorizeTokenHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {

    private final String userToken;

    @Inject
    public AuthorizeTokenHttpJsonRequestFactory(@Named("user.token") String token) {
        userToken = token;
    }

    @Override
    public HttpJsonRequest fromUrl(@NotNull String url) {
        return super.fromUrl(url).setAuthorizationHeader(userToken);
    }

    @Override
    public HttpJsonRequest fromLink(@NotNull Link link) {
        return super.fromLink(link).setAuthorizationHeader(userToken);
    }
}
