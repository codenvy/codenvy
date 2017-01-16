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
import com.codenvy.client.store.DataStore;
import com.codenvy.client.store.DataStoreFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link DataStoreFactory} implementation providing {@link DataStore} which stores user credentials in memory.
 *
 * @author Kevin Pollet
 */
public class InMemoryDataStoreFactory implements DataStoreFactory<String, Credentials> {
    private final ConcurrentMap<String, DataStore<String, Credentials>> dataStores;

    public InMemoryDataStoreFactory() {
        this.dataStores = new ConcurrentHashMap<>();
    }

    @Override
    public DataStore<String, Credentials> getDataStore(String id) {
        checkNotNull(id);

        DataStore<String, Credentials> store = dataStores.get(id);
        if (store == null) {
            final DataStore<String, Credentials> dataStore = new InMemoryDataStore();
            store = dataStores.putIfAbsent(id, dataStore);
            if (store == null) {
                store = dataStore;
            }
        }
        return store;
    }
}
