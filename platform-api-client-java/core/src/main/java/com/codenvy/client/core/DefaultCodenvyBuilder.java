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

import com.codenvy.client.Codenvy;
import com.codenvy.client.CodenvyBuilder;
import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.CredentialsProvider;
import com.codenvy.client.core.store.InMemoryDataStoreFactory;
import com.codenvy.client.store.DataStoreFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder used to build a {@link Codenvy} client.
 *
 * @author Kevin Pollet
 */
public class DefaultCodenvyBuilder implements CodenvyBuilder {
    private final String                                url;
    private final String                                username;
    private       Credentials                           credentials;
    private       CredentialsProvider                   credentialsProvider;
    private       DataStoreFactory<String, Credentials> credentialsStoreFactory;

    /**
     * Constructs an instance of {@link CodenvyBuilder}.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param username
     *         the user name.
     * @throws NullPointerException
     *         if url or username parameter is {@code null}.
     */
    public DefaultCodenvyBuilder(String url, String username) {
        checkNotNull(url);
        checkNotNull(username);

        this.url = url;
        this.username = username;
        this.credentialsStoreFactory = new InMemoryDataStoreFactory();
    }

    /**
     * Provides the user {@link Credentials} used if they are not found in storage.
     *
     * @param credentials
     *         the provided {@link Credentials}.
     * @return {@link CodenvyBuilder} instance.
     * @throws IllegalArgumentException
     *         if provided {@link Credentials} doesn't match the given username.
     */
    @Override
    public CodenvyBuilder withCredentials(Credentials credentials) {
        checkArgument(username.equals(credentials.username()));

        this.credentials = credentials;
        return this;
    }

    /**
     * Defines the {@link DataStoreFactory} used to store the user {@link Credentials}.
     *
     * @param credentialsStoreFactory
     *         the {@link DataStoreFactory} to use.
     * @return {@link CodenvyBuilder} instance.
     */
    @Override
    public CodenvyBuilder withCredentialsStoreFactory(DataStoreFactory<String, Credentials> credentialsStoreFactory) {
        this.credentialsStoreFactory = credentialsStoreFactory;
        return this;
    }

    /**
     * Defines the {@link CredentialsProvider} used to provide credentials if they are not stored or provided
     *
     * @param credentialsProvider
     *         the credentials provider.
     * @return {@link CodenvyBuilder} instance.
     */
    @Override
    public CodenvyBuilder withCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    /**
     * Builds the {@link Codenvy} client.
     *
     * @return the {@link Codenvy} client instance.
     */
    @Override
    public Codenvy build() {
        return new DefaultCodenvy(url, username, credentials, credentialsProvider, credentialsStoreFactory);
    }
}