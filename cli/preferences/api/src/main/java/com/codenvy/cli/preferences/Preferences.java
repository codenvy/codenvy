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
package com.codenvy.cli.preferences;

import java.util.Map;

/**
 * <p>
 * A preferences node in a preferences hierarchy. Give all the abilities to read and write preferences. The way preferences are stored
 * (in-memory, file based or other) is let at the choice of the implementer.
 * </p>
 * <p>
 * Preferences are stored in a treeish way. Leaves are primitives types, {@link String} or {@link Enum}. All other values stored are to be
 * mapped to build a hierarchical tree.
 * </p>
 *
 * @author Stéphane Daviet
 */
public interface Preferences {
    /**
     * Read the given {@code key} from the underlying store, like in the {@link Map#get(Object)} way, but mapping the underlying retrieved
     * {@link Object} to the {@link Class} passed as {@code clazz} parameter.
     *
     * @param key the search key.
     * @param clazz the {@link Class} on which to map the retrieved object.
     * @return a result of mapped from the underlying retrieved object to the specified {@link Class}.
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * <p>
     * Write the given {@code value} to the underlying store associated with the given {@code key}, like in the
     * {@link Map#put(Object, Object)} way. Mapping can trigger specific actions in the underlying implementation to for instance persist
     * permanently the info.
     * </p>
     * <p>
     * <strong>This method works in a overwrite way.</strong> It means that all pre-existing content under the given key is overwritten by
     * the value. If needed, the {@link #merge(String, Object)} operates in a less destructive manner, merging the passed {@code value} with
     * the pre-existing content.
     * </p>
     *
     * @param key the key with which the specified {@code value} will be associated.
     * @param value the value to store.
     * @see #merge(String, Object)
     */
    void put(String key, Object value);

    /**
     * <p>
     * Like {@link #put(String, Object)} but merges the value with an potential already existing content.
     * </p>
     *
     * @param key the key with which the specified {@code value} will be associated.
     * @param value the value to store.
     * @see #put(String, Object)
     */
    void merge(String key, Object value);

    /**
     * Delete the content under the given {@code key}.
     *
     * @param key the key under which the content will be deleted.
     */
    void delete(String key);

    /**
     * Walk a pre-existing path in the preferences tree or create a node for the given key either. It gives the ability to go one level
     * deeper in the preferences tree. To walk the tree without triggering node creation, use {@link #walk(String)} instead.
     *
     * @param key the key to navigate in the preferences tree.
     * @return a {@link Preferences} one level deeper in the preferences tree for the give {@code key}.
     * @see #walk(String)
     */
    Preferences path(String key);

    /**
     * Like {@link #path(String)} but without node creation.
     *
     * @param key the key to navigate in the preferences tree.
     * @return a {@link Preferences} one level deeper in the preferences tree for the give {@code key} or {@code null} if none exists.
     */
    Preferences walk(String key);

    /**
     * Test if a node exist for the given key in the preferences tree.
     *
     * @param key the key to navigate in the preferences tree.
     * @return {@code true} if node exists, {@code false} either.
     */
    boolean pathExists(String key);
}
