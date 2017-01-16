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

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Entry point to get the {@link com.codenvy.cli.preferences.Preferences} instance.
 * @author Florent Benoit
 */
public class PreferencesAPI {

    /**
     * Initialized ?
     */
    private static AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * List of preferences manager.
     */
    private static List<PreferencesProvider> preferencesProviders = new CopyOnWriteArrayList<>();

    public static void addPreferencesProvider(PreferencesProvider preferencesProvider) {
        preferencesProviders.add(preferencesProvider);
    }

    public static void removePreferencesProvider(PreferencesProvider preferencesProvider) {
        preferencesProviders.remove(preferencesProvider);
    }


    /**
     * @return the {@link PreferencesProvider} instance.
     */
    public static Preferences getPreferences(URI uri) {
        if (!initialized.get()) {
            // Use context classloader to find implementation
            ServiceLoader<PreferencesProvider> preferencesServiceLoader =
                    ServiceLoader.load(PreferencesProvider.class, Thread.currentThread().getContextClassLoader());

            Iterator<PreferencesProvider> iterator = preferencesServiceLoader.iterator();

            // take all
            while (iterator.hasNext()) {
                addPreferencesProvider(iterator.next());
            }

            // Init done
            initialized.set(true);
      }

        for (PreferencesProvider preferencesProvider : preferencesProviders) {
            Preferences prefs = preferencesProvider.buildPreferences(uri);
            if (prefs != null) {
                return prefs;
            }
        }

        if (preferencesProviders.isEmpty()) {
            throw new IllegalStateException("Unable to find an implementation of '" + PreferencesProvider.class.getName() +
                                            "'. Check Implementation bundle is available on the platform or that implementation jar contains META-INF/services/src/main/resources/META-INF/services/com.codenvy.cli.preferences.PreferencesManager key.");
        }

        return null;
    }


}
