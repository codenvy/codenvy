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
package com.codenvy.cli.security;

import com.codenvy.cli.preferences.Preferences;
import com.codenvy.client.CodenvyClient;
import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.CredentialsProvider;
import com.codenvy.client.store.DataStore;
import com.codenvy.client.store.DataStoreFactory;

/**
 * Factory that has only a mapping one-to-one as we're one factory for each URL/user
 *
 * @author Florent Benoit
 */
public class PreferencesDataStore implements DataStoreFactory<String, Credentials>, DataStore<String, Credentials>, CredentialsProvider {

    /**
     * Preferences used to read data.
     */
    private Preferences preferences;

    /**
     * Helper.
     */
    private CredentialsHelper credentialsHelper;

    /**
     * Nma of the remote
     */
    private String remote;

    /**
     * Basic constructor that initializes an empty {@link DataStore} cache.
     */
    public PreferencesDataStore(Preferences preferences, String remote, CodenvyClient codenvyClient) {
        this.preferences = preferences;
        this.remote = remote;
        this.credentialsHelper = new CredentialsHelper(codenvyClient);
    }


    // Navigate to the right preferences node
    private RemoteCredentials getRemoteCredentials() {
        return this.preferences.get(remote, RemoteCredentials.class);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Get the {@link Credentials} stored for a given key. {@link Credentials} is retrieved from the in-memory cache.
     * </p>
     */
    @Override
    public Credentials get(String key) {
        return credentialsHelper.convert(getRemoteCredentials());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Store the {@link Credentials} first in the in-memory cache, then dump this cache to the file. This method is synchronized to prevent
     * multiple threads from writing the file simultaneously.
     * </p>
     */
    @Override
    public synchronized Credentials put(String key, Credentials credentials) {
        this.preferences.put(remote, credentialsHelper.convert(credentials));
        return get(key);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Same than {@link #put(String, Credentials)} for the mechanism: {@link Credentials} is deleted for the given key, then the file is
     * dumped.
     * </p>
     */
    @Override
    public Credentials delete(String key) {


        // we delete the token in this case
        RemoteCredentials empty = new RemoteCredentials();
        empty.setToken("");
        preferences.merge(remote, empty);

        return credentialsHelper.convert(getRemoteCredentials());
    }

    @Override
    public DataStore<String, Credentials> getDataStore(String s) {
        // ignore the URL mapping as we're only having one store per factory
        return this;
    }

    @Override
    public Credentials getCredentials(String s) {
        return credentialsHelper.convert(getRemoteCredentials());
    }
}
