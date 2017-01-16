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
package com.codenvy.client.core.store;

import com.codenvy.client.auth.Credentials;
import com.codenvy.client.core.auth.DefaultCredentialsBuilder;
import com.codenvy.client.store.DataStore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link DataStore} implementation which stores user credentials in memory.
 *
 * @author Kevin Pollet
 */
public class InMemoryDataStore implements DataStore<String, Credentials> {
    private final ConcurrentMap<String, Credentials> data;

    InMemoryDataStore() {
        this.data = new ConcurrentHashMap<>();
    }

    @Override
    public Credentials get(String key) {
        checkNotNull(key);

        return data.get(key);
    }

    @Override
    public Credentials put(String key, Credentials credentials) {
        checkNotNull(key);
        checkNotNull(credentials);

        return data.putIfAbsent(key,
                                credentials.isStoreOnlyToken() ? new DefaultCredentialsBuilder().withToken(credentials.token())
                                                                                                .build()
                                                               : credentials);
    }

    @Override
    public Credentials delete(String key) {
        checkNotNull(key);

        return data.remove(key);
    }
}
