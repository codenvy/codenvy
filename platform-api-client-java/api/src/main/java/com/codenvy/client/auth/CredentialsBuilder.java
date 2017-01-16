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
package com.codenvy.client.auth;

/**
 * Builder used to build a {@link Credentials} object.
 *
 * @author Florent Benoit
 */
public interface CredentialsBuilder {

    /**
     * Defines the user name.
     *
     * @param username
     *         the user name.
     * @return the {@link com.codenvy.client.auth.CredentialsBuilder} instance.
     */
    CredentialsBuilder withUsername(String username);

    /**
     * Defines the user password.
     *
     * @param password
     *         the user password.
     * @return the {@link com.codenvy.client.auth.CredentialsBuilder} instance.
     */
    CredentialsBuilder withPassword(String password);

    /**
     * Defines the authentication {@link com.codenvy.client.auth.Token}.
     *
     * @param token
     *         the authentication {@link com.codenvy.client.auth.Token}.
     * @return the {@link com.codenvy.client.auth.CredentialsBuilder} instance.
     */
    CredentialsBuilder withToken(Token token);

    /**
     * Defines if only the authentication {@link Token} must be stored.
     *
     * @param storeOnlyToken
     *         {@code true} if only the authentication {@link com.codenvy.client.auth.Token} must be stored.
     * @return the {@link com.codenvy.client.auth.CredentialsBuilder} instance.
     */
    CredentialsBuilder storeOnlyToken(boolean storeOnlyToken);

    /**
     * Builds the {@link com.codenvy.client.auth.Credentials} object.
     *
     * @return the created {@link com.codenvy.client.auth.Credentials} object.
     */
    Credentials build();
}
