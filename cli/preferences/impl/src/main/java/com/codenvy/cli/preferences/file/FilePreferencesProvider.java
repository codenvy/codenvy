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
package com.codenvy.cli.preferences.file;

import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.preferences.PreferencesProvider;

import java.io.File;
import java.net.URI;

/**
 * Default implementation that will provide instances of preferences based on file.
 * @author Florent Benoit
 */
public class FilePreferencesProvider implements PreferencesProvider {

    /**
     * Build and return a new preferences based on the given URI.
     *
     * @param uri
     *         the URI that indicates the URI wanted
     * @return the newly built instance
     */
    public Preferences buildPreferences(URI uri) {
        if (!"file".equals(uri.getScheme())) {
            return null;
        }
        return new FilePreferences(new File(uri));
    }
}
