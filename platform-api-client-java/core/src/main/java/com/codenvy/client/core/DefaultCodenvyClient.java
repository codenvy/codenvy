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
package com.codenvy.client.core;

import com.codenvy.client.CodenvyBuilder;
import com.codenvy.client.CodenvyClient;
import com.codenvy.client.auth.CredentialsBuilder;
import com.codenvy.client.auth.TokenBuilder;
import com.codenvy.client.core.auth.DefaultCredentialsBuilder;
import com.codenvy.client.core.auth.DefaultTokenBuilder;
import com.codenvy.client.core.model.DefaultProjectBuilder;
import com.codenvy.client.core.model.runner.DefaultRunOptionsBuilder;
import com.codenvy.client.model.ProjectBuilder;
import com.codenvy.client.model.runner.RunOptionsBuilder;

/**
 * Default implementation of {@link com.codenvy.client.CodenvyClient} interface.
 *
 * @author Florent Benoit
 */
public class DefaultCodenvyClient implements CodenvyClient {

    /**
     * @return {@link com.codenvy.client.auth.CredentialsBuilder}
     */
    public CredentialsBuilder newCredentialsBuilder() {
        return new DefaultCredentialsBuilder();
    }

    /**
     * Build a new Codenvy builder that will build {@link com.codenvy.client.Codenvy} object connected to Codenvy platform.
     *
     * @param url
     *         the URL to connect to the system
     * @param username
     *         the username to use to the connection
     * @return {@link com.codenvy.client.CodenvyBuilder}
     */
    public CodenvyBuilder newCodenvyBuilder(String url, String username) {
        return new DefaultCodenvyBuilder(url, username);
    }

    /**
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    public ProjectBuilder newProjectBuilder() {
        return new DefaultProjectBuilder();
    }

    /**
     * @param value
     *         the value of the token
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    @Override
    public TokenBuilder newTokenBuilder(String value) {
        return new DefaultTokenBuilder(value);
    }

    /**
     * @return {@link com.codenvy.client.model.runner.RunOptionsBuilder} instance
     */
    @Override
    public RunOptionsBuilder newRunOptionsBuilder() {
        return new DefaultRunOptionsBuilder();
    }
}
