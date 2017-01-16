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

import com.codenvy.client.BuilderClient;
import com.codenvy.client.Codenvy;
import com.codenvy.client.FactoryClient;
import com.codenvy.client.GitClient;
import com.codenvy.client.UserClient;
import com.codenvy.client.VFSClient;
import com.codenvy.client.VersionClient;
import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.CredentialsProvider;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.store.DataStore;
import com.codenvy.client.store.DataStoreFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Codenvy client API entry point.
 *
 * @author Kevin Pollet
 */
public class DefaultCodenvy implements Codenvy {

    private final String                url;
    private final AuthenticationManager authenticationManager;

    /**
     * Constructs an instance of {@link DefaultCodenvy} client API.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param username
     *         the username.
     * @param credentials
     *         the provided user {@link com.codenvy.client.core.auth.DefaultCredentials} might be {@code null}.
     * @param credentialsStoreFactory
     *         the {@link DataStoreFactory}.
     * @param credentialsProvider
     *         provider used to provide credentials if they are not stored or provided.
     * @throws NullPointerException
     *         if url, username or credentialsProvider parameter is {@code null}.
     */
    protected DefaultCodenvy(String url,
                             String username,
                             Credentials credentials,
                             CredentialsProvider credentialsProvider,
                             DataStoreFactory<String, Credentials> credentialsStoreFactory) {

        checkNotNull(url);
        checkNotNull(username);

        this.url = url;

        final DataStore<String, Credentials> credentialsStore = credentialsStoreFactory.getDataStore(url);
        this.authenticationManager = new AuthenticationManager(url, username, credentials, credentialsProvider, credentialsStore);
    }

    /**
     * Returns the user API client.
     *
     * @return the user API client.
     */
    @Override
    public UserClient user() {
        return new DefaultUserClient(url, authenticationManager);
    }

    /**
     * Returns the builder API client.
     *
     * @return the builder API client.
     */
    @Override
    public BuilderClient builder() {
        return new DefaultBuilderClient(url, authenticationManager);
    }

    /**
     * Returns the runner API client.
     *
     * @return the runner API client.
     */
    @Override
    public DefaultRunnerClient runner() {
        return new DefaultRunnerClient(url, authenticationManager);
    }

    /**
     * Returns the project API client.
     *
     * @return the project API client.
     */
    @Override
    public DefaultProjectClient project() {
        return new DefaultProjectClient(url, authenticationManager);
    }

    /**
     * Returns the workspace API client.
     *
     * @return the workspace API client.
     */
    @Override
    public DefaultWorkspaceClient workspace() {
        return new DefaultWorkspaceClient(url, authenticationManager);
    }

    /**
     * Returns the version API client.
     *
     * @return the version API client.
     */
    @Override
    public VersionClient version() {
        return new DefaultVersionClient(url, authenticationManager);
    }

    /**
     * Returns the factory API client.
     *
     * @return the factory API client.
     */
    @Override
    public FactoryClient factory() {
        return new DefaultFactoryClient(url, authenticationManager);
    }

    /**
     * Returns the Git API client.
     *
     * @return the Git API client.
     */
    @Override
    public GitClient git() {
        return new DefaultGitClient(url, authenticationManager);
    }

    /**
     * Returns the factory API client.
     *
     * @return the factory API client.
     */
    @Override
    public VFSClient vfs() {
        return new DefaultVFSClient(url, authenticationManager);
    }


}
