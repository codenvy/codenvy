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
package com.codenvy.client;

import com.codenvy.client.auth.CredentialsBuilder;
import com.codenvy.client.auth.TokenBuilder;
import com.codenvy.client.model.ProjectBuilder;
import com.codenvy.client.model.runner.RunOptionsBuilder;

/**
 * Get builders for creating Codenvy objects.
 *
 * @author Florent Benoit
 */
public interface CodenvyClient {

    /**
     * @return {@link com.codenvy.client.auth.CredentialsBuilder}
     */
    CredentialsBuilder newCredentialsBuilder();

    /**
     * Build a new Codenvy builder that will build {@link Codenvy} object connected to Codenvy platform.
     *
     * @param url
     *         the URL to connect to the system
     * @param username
     *         the username to use to the connection
     * @return {@link com.codenvy.client.CodenvyBuilder}
     */
    CodenvyBuilder newCodenvyBuilder(String url, String username);

    /**
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    ProjectBuilder newProjectBuilder();

    /**
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    TokenBuilder newTokenBuilder(String value);

    /**
     * @return {@link com.codenvy.client.model.runner.RunOptionsBuilder} instance
     */
    RunOptionsBuilder newRunOptionsBuilder();

}
