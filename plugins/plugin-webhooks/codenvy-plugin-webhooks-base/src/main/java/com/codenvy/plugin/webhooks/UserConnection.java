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
