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

/**
 * Preferences manager allows to get instance of preferences that will be managed in a given {@link URI}
 *
 * @author Florent Benoit
 */
public interface PreferencesProvider {

    /**
     * Build and return a new preferences based on the given URI.
     * If no provider for the given scheme has been found, it will return null
     * @param uri
     *         the URI that indicates the URI wanted
     * @return the newly built instance
     */
    Preferences buildPreferences(URI uri);
}
