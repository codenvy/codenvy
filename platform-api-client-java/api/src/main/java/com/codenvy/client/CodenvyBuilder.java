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
package com.codenvy.client;

import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.CredentialsProvider;
import com.codenvy.client.store.DataStoreFactory;

/**
 * Builder used to build a {@link com.codenvy.client.Codenvy} client.
 *
 * @author Florent Benoit
 */
public interface CodenvyBuilder {

    /**
     * Provides the user {@link Credentials} used if they are not found in storage.
     *
     * @param credentials
     *         the provided {@link Credentials}.
     * @return {@link CodenvyBuilder} instance.
     * @throws IllegalArgumentException
     *         if provided {@link Credentials} doesn't match the given username.
     */
    CodenvyBuilder withCredentials(Credentials credentials);

    /**
     * Defines the {@link DataStoreFactory} used to store the user {@link Credentials}.
     *
     * @param credentialsStoreFactory
     *         the {@link DataStoreFactory} to use.
     * @return {@link CodenvyBuilder} instance.
     */
    CodenvyBuilder withCredentialsStoreFactory(DataStoreFactory<String, Credentials> credentialsStoreFactory);

    /**
     * Defines the {@link CredentialsProvider} used to provide credentials if they are not stored or provided
     *
     * @param credentialsProvider
     *         the credentials provider.
     * @return {@link CodenvyBuilder} instance.
     */
    CodenvyBuilder withCredentialsProvider(CredentialsProvider credentialsProvider);

    /**
     * Builds the {@link com.codenvy.client.Codenvy} client.
     *
     * @return the {@link com.codenvy.client.Codenvy} client instance.
     */
    Codenvy build();
}
