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
package com.codenvy.client.dummy;

import com.codenvy.client.CodenvyBuilder;
import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.CredentialsProvider;
import com.codenvy.client.store.DataStoreFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Florent Benoit
 */
public class DummyCodenvyBuilder implements CodenvyBuilder {

    /**
     * Map of builders (URL --> Codenvy)
     */
    private static Map<String, DummyCodenvy> codenvyMap = new HashMap<>();


    private String url;

    public DummyCodenvyBuilder(String url, String username) {
        this.url = url;
        codenvyMap.put(url, new DummyCodenvy());
    }



    /**
     * Provides the user {@link com.codenvy.client.auth.Credentials} used if they are not found in storage.
     *
     * @param credentials
     *         the provided {@link com.codenvy.client.auth.Credentials}.
     * @return {@link com.codenvy.client.CodenvyBuilder} instance.
     * @throws IllegalArgumentException
     *         if provided {@link com.codenvy.client.auth.Credentials} doesn't match the given username.
     */
    @Override
    public DummyCodenvyBuilder withCredentials(Credentials credentials) {
        return this;
    }

    /**
     * Defines the {@link com.codenvy.client.store.DataStoreFactory} used to store the user {@link com.codenvy.client.auth.Credentials}.
     *
     * @param credentialsStoreFactory
     *         the {@link com.codenvy.client.store.DataStoreFactory} to use.
     * @return {@link com.codenvy.client.CodenvyBuilder} instance.
     */
    @Override
    public DummyCodenvyBuilder withCredentialsStoreFactory(DataStoreFactory<String, Credentials> credentialsStoreFactory) {
        return this;
    }

    /**
     * Defines the {@link com.codenvy.client.auth.CredentialsProvider} used to provide credentials if they are not stored or provided
     *
     * @param credentialsProvider
     *         the credentials provider.
     * @return {@link com.codenvy.client.CodenvyBuilder} instance.
     */
    @Override
    public DummyCodenvyBuilder withCredentialsProvider(CredentialsProvider credentialsProvider) {
        return this;
    }

    /**
     * Builds the {@link com.codenvy.client.Codenvy} client.
     *
     * @return the {@link com.codenvy.client.Codenvy} client instance.
     */
    @Override
    public DummyCodenvy build() {
        return codenvyMap.get(url);
    }
}
