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
 * Store contract used to store user credentials.
 *
 * @param <K>
 *         the key {@linkplain java.lang.reflect.Type Type}.
 * @param <V>
 *         the value {@linkplain java.lang.reflect.Type Type}.
 * @author Kevin Pollet
 */
public interface DataStore<K, V> {
    /**
     * Returns te value associated to the given key.
     *
     * @param key
     *         the key.
     * @return the associated value or {@code null} if none.
     * @throws NullPointerException
     *         if implementation doesn't support {@code null} keys.
     */
    V get(K key);

    /**
     * Stores the given value with the given key.
     *
     * @param key
     *         the key.
     * @param value
     *         the value.
     * @return the previous stored value or {@code null} if none.
     * @throws NullPointerException
     *         if implementation doesn't support {@code null} keys or values.
     */
    V put(K key, V value);

    /**
     * Deletes the value associated to the given key.
     *
     * @param key
     *         the key.
     * @return the deleted value or {@code null} if none.
     * @throws NullPointerException
     *         if implementation doesn't support {@code null} keys.
     */
    V delete(K key);
}
