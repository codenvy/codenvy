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
package com.codenvy.client.core.auth;

import com.codenvy.client.auth.CodenvyAuthenticationException;
import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.CredentialsProvider;
import com.codenvy.client.auth.Token;
import com.codenvy.client.store.DataStore;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Authentication manager used to authenticate an user with the Codenvy platform.
 *
 * @author Kevin Pollet
 */
public class AuthenticationManager {
    private final String                         username;
    private final Credentials                    credentials;
    private final WebTarget                      webTarget;
    private final DataStore<String, Credentials> dataStore;
    private final CredentialsProvider            credentialsProvider;

    /**
     * Constructs an instance of {@link AuthenticationManager}.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param username
     *         the user name.
     * @param credentials
     *         the provided {@link Credentials}.
     * @param credentialsProvider
     *         provider used to provide credentials if they are not stored or provided.
     * @param dataStore
     *         the {@link DataStore} used to store the user {@link Credentials}.
     * @throws NullPointerException
     *         if url or username parameter is {@code null}.
     */
    public AuthenticationManager(String url,
                                 String username,
                                 Credentials credentials,
                                 CredentialsProvider credentialsProvider,
                                 DataStore<String, Credentials> dataStore) {

        checkNotNull(url);
        checkNotNull(username);

        this.dataStore = dataStore;
        this.username = username;
        this.credentials = credentials;
        this.credentialsProvider = credentialsProvider;

        final URI loginURI = UriBuilder.fromUri(url)
                                       .path("api")
                                       .path("auth")
                                       .path("login")
                                       .build();

        this.webTarget = ClientBuilder.newClient()
                                      .target(loginURI)
                                      .register(JacksonJsonProvider.class);
    }

    /**
     * Authorises the contextual user with the Codenvy platform.
     *
     * @return the authentication {@link Token}.
     * @throws com.codenvy.client.auth.CodenvyAuthenticationException
     *         if there is a problem during the token negotiation.
     */
    public Token authorize() throws CodenvyAuthenticationException {
        return authorize(credentials);
    }

    /**
     * Authorises the user with the following {@link Credentials} on Codenvy platform.
     *
     * @param credentials
     *         the user {@link Credentials}.
     * @return the authentication {@link Token}.
     * @throws CodenvyAuthenticationException
     *         if there is a problem during the token negotiation.
     */
    private Token authorize(Credentials credentials) throws CodenvyAuthenticationException {
        if (credentials == null || credentials.password() == null) {
            if (credentialsProvider != null) {
                credentials = credentialsProvider.getCredentials(username);
            }
            if (credentials == null || credentials.password() == null) {
                throw new CodenvyAuthenticationException("No credentials provided for authentication");
            }
        }

        final Response response = webTarget.request()
                                           .accept(APPLICATION_JSON)
                                           .post(json(credentials));

        DefaultToken token = null;

        if (Status.fromStatusCode(response.getStatus()) == Status.OK) {
            token = response.readEntity(DefaultToken.class);

            if (dataStore != null) {
                storeCredentials(credentials, token);
            }
        }

        if (token == null) {
            throw new CodenvyAuthenticationException(
                    "Unable to negotiate a token for authentication :" + response.getStatusInfo().toString());
        }

        return token;
    }

    /**
     * Retrieves the stored Codenvy API {@link Token} for the given user.
     *
     * @return the {@link Token} or {@code null} if none.
     */
    public Token getToken() {
        if (dataStore == null) {
            return null;
        }

        final Credentials storedCredentials = dataStore.get(username);
        if (credentials != null && storedCredentials != null) {
            storeCredentials(credentials, storedCredentials.token());
        }

        return storedCredentials == null ? null : storedCredentials.token();
    }

    /**
     * Refresh the the Codenvy API {@link Token} for the given user.
     *
     * @return the {@link Token}.
     * @throws CodenvyAuthenticationException
     *         if there is a problem during the token negotiation.
     */
    public Token refreshToken() throws CodenvyAuthenticationException {
        return authorize(dataStore == null ? null : dataStore.get(username));
    }


    /**
     * Stores the given {@link DefaultCredentials} in the {@link DataStore}.
     *
     * @param credentials
     *         the {@link DefaultCredentials} to store.
     * @param token
     *         the negotiated or retrieved {@link DefaultToken} to store.
     */
    private void storeCredentials(Credentials credentials, Token token) {
        final Credentials credentialsToStore = new DefaultCredentialsBuilder().withUsername(credentials.username())
                                                                              .withPassword(credentials.password())
                                                                              .withToken(token)
                                                                              .storeOnlyToken(credentials.isStoreOnlyToken())
                                                                              .build();

        dataStore.put(username, credentialsToStore);
    }
}
