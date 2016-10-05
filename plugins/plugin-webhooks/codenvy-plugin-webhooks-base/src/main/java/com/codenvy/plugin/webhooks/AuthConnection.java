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

import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * Wrapper class for calls to Codenvy auth REST API
 *
 * @author Stephane Tournie
 */
public class AuthConnection {

    private static final Logger LOG = LoggerFactory.getLogger(AuthConnection.class);

    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final String                 baseUrl;

    @Inject
    public AuthConnection(HttpJsonRequestFactory httpJsonRequestFactory, @Named("che.api") String baseUrl) {
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.baseUrl = baseUrl;
    }

    /**
     * Authenticate against Codenvy
     *
     * @param username
     *         the username of the user to getByAliasAndPassword
     * @param password
     *         the password of the user to getByAliasAndPassword
     * @return an auth token if authentication is successful, null otherwise
     * @throws ServerException
     */
    public Token authenticateUser(String username, String password) throws ServerException {
        Token userToken;
        String url = fromUri(baseUrl).path(AuthenticationService.class).path(AuthenticationService.class, "authenticate")
                                     .build().toString();

        Credentials credentials = DtoFactory.newDto(Credentials.class).withUsername(username).withPassword(password);
        HttpJsonRequest httpJsonRequest = httpJsonRequestFactory.fromUrl(url).usePostMethod().setBody(credentials);
        try {
            HttpJsonResponse response = httpJsonRequest.request();
            userToken = response.asDto(Token.class);

        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        if (userToken != null) {
            LOG.debug("successfully authenticated with token {}", userToken);
        }
        return userToken;
    }
}
