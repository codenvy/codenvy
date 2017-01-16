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
package com.codenvy.cli.security;

import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.Token;
import com.codenvy.client.store.DataStore;
import com.codenvy.client.store.DataStoreFactory;

/**
 * @author Florent Benoit
 */
public class TokenRetrieverDatastore implements DataStoreFactory<String, Credentials>, DataStore<String, Credentials> {

    private Token token;
    private Credentials credentials;


    @Override
    public Credentials get(String s) {
        return this.credentials;
    }

    @Override
    public Credentials put(String s, Credentials credentials) {
        if (credentials != null) {
            this.token = credentials.token();
            this.credentials = credentials;
        }
        return credentials;
    }

    @Override
    public Credentials delete(String s) {
        return null;
    }

    @Override
    public DataStore<String, Credentials> getDataStore(String s) {
        return this;
    }

    public Token getToken() {
        return token;
    }
}
