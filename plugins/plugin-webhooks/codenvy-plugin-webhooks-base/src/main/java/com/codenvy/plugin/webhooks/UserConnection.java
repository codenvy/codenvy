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
package com.codenvy.plugin.webhooks;


import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * Wrapper class for calls to Codenvy user REST API
 *
 * @author Stephane Tournie
 */
public class UserConnection {

    private static final Logger LOG = LoggerFactory.getLogger(UserConnection.class);

    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final String                 baseUrl;

    @Inject
    public UserConnection(HttpJsonRequestFactory httpJsonRequestFactory, @Named("che.api") String baseUrl) {
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.baseUrl = baseUrl;
    }

    /**
     * Get current user
     *
     * @return the current user or null if an error occurred during the call to 'getCurrent'
     * @throws org.eclipse.che.api.core.ServerException
     */
    public UserDto getCurrentUser() throws ServerException {
        String url = fromUri(baseUrl).path(UserService.class).path(UserService.class, "getCurrent").build().toString();
        UserDto user;
        HttpJsonRequest httpJsonRequest = httpJsonRequestFactory.fromUrl(url).useGetMethod();
        try {
            HttpJsonResponse response = httpJsonRequest.request();
            user = response.asDto(UserDto.class);

        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        return user;
    }
}
