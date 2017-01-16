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
package com.codenvy.client.dummy.auth;

import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.Token;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User credentials used for authentication.
 *
 * @author Florent Benoit
 */
public class DummyCredentials implements Credentials {

    /**
     * Username used by this credentials.
     */
    private String username;

    /**
     * Password of this credentials
     */
    private String password;

    /**
     * Token of this credentials
     */
    private Token token;

    /**
     * True if the token is only for storing, else false.
     */
    private boolean storeOnlyToken;

    /**
     * Constructs an instance of {@link DummyCredentials}.
     *
     * @param username
     *         the user name.
     * @param password
     *         the user password.
     * @param token
     *         the user authentication {@link com.codenvy.client.auth.Token}.
     * @param storeOnlyToken
     *         {@code true} if only the authentication {@link com.codenvy.client.auth.Token} must be stored.
     */
    protected DummyCredentials(String username, String password, Token token, boolean storeOnlyToken) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.storeOnlyToken = storeOnlyToken;
    }

    /**
     * @return hash code of this object
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    /**
     * Returns true if the other object is equals to this object
     *
     * @param obj
     *         the other object
     * @return true if obj and this are equals, else false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DummyCredentials other = (DummyCredentials)obj;
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.equals(other.token)) {
            return false;
        }
        if (username == null) {
            if (other.username != null) {
                return false;
            }
        } else if (!username.equals(other.username)) {
            return false;
        }
        return true;
    }


    /**
     * @return username of this credentials
     */
    @Override
    public String username() {
        return username;
    }


    /**
     * Sets the username of this credentials
     *
     * @param username
     *         the user name of this credentials
     * @return {@link com.codenvy.client.auth.Credentials}
     */
    public Credentials withUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * @return password of this credentials
     */
    @Override
    public String password() {
        return password;
    }

    /**
     * Sets the password of this credentials
     *
     * @param password
     *         the password of this credentials
     * @return {@link com.codenvy.client.auth.Credentials}
     */
    public Credentials withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * @return token of this credentials
     */
    @Override
    public Token token() {
        return token;
    }

    /**
     * Sets the token of this credentials
     *
     * @param token
     *         the token of this credentials
     * @return {@link com.codenvy.client.auth.Credentials}
     */
    public Credentials withToken(Token token) {
        this.token = token;
        return this;
    }

    /**
     * @return true if the token of this credentials needs to be only stored
     */
    @Override
    public boolean isStoreOnlyToken() {
        return storeOnlyToken;
    }

    /**
     * Sets the flag for storing the token of this credentials
     *
     * @param storeOnlyToken
     *         the boolean value for storing or not the token of this credentials
     * @return {@link com.codenvy.client.auth.Credentials}
     */
    public Credentials withStoreOnlyToken(boolean storeOnlyToken) {
        this.storeOnlyToken = storeOnlyToken;
        return this;
    }
}
