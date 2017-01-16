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
package com.codenvy.client.store;

/**
 * Data store factory contract used to store user credentials.
 *
 * @param <K>
 *         the key {@linkplain java.lang.reflect.Type Type}.
 * @param <V>
 *         the value {@linkplain java.lang.reflect.Type Type}.
 * @author Kevin Pollet
 */
public interface DataStoreFactory<K, V> {
    /**
     * Returns an instance of the data store associated to the given id.
     *
     * @param id
     *         the data store id.
     * @return the associated data store.
     * @throws NullPointerException
     *         if id parameter is {@code null}.
     */
    DataStore<K, V> getDataStore(String id);
}
