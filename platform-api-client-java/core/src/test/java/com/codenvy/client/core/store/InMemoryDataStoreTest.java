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
package com.codenvy.client.core.store;

import com.codenvy.client.auth.Credentials;
import com.codenvy.client.core.auth.DefaultCredentialsBuilder;
import com.codenvy.client.core.auth.DefaultToken;
import com.codenvy.client.store.DataStore;
import com.codenvy.client.store.DataStoreFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link InMemoryDataStore} tests.
 * 
 * @author Kevin Pollet
 */
public class InMemoryDataStoreTest {
    private static final String DUMMY_DATASTORE_KEY = "dummyKey";
    private static final String DUMMY_USERNAME      = "dummyUsername";
    private static final String DUMMY_PASSWORD      = "dummyPassword";
    private static final String DUMMY_TOKEN         = "dummyToken";

    private DataStore<String, Credentials> dataStore;
    private Credentials                    credentials;

    @Before
    public void before() {
        DataStoreFactory<String, Credentials> dataStoreFactory = new InMemoryDataStoreFactory();
        this.dataStore = dataStoreFactory.getDataStore(DUMMY_DATASTORE_KEY);
        this.credentials = new DefaultCredentialsBuilder().withPassword(DUMMY_PASSWORD)
                                                           .withToken(new DefaultToken(DUMMY_TOKEN))
                                                           .build();
    }

    @Test(expected = NullPointerException.class)
    public void testGetWithNullKey() {
        dataStore.get(null);
    }

    @Test
    public void testGetWithMissingKey() {
        Assert.assertNull(dataStore.get("missing"));
    }

    @Test
    public void testGetWithExistingKey() {
        dataStore.put("testGetWithExistingKey", credentials);

        Assert.assertNotNull(dataStore.get("testGetWithExistingKey"));
        Assert.assertEquals(credentials, dataStore.get("testGetWithExistingKey"));
    }

    @Test(expected = NullPointerException.class)
    public void testPutWithNullKey() {
        dataStore.put(null, credentials);
    }

    @Test(expected = NullPointerException.class)
    public void testPutWithNullCredentials() {
        dataStore.put(DUMMY_USERNAME, null);
    }

    @Test
    public void testPutWithStoreOnlyTokenCredentials() {
        final Credentials credentials = new DefaultCredentialsBuilder().withPassword(DUMMY_PASSWORD)
                                                                 .withToken(new DefaultToken(DUMMY_TOKEN))
                                                                 .storeOnlyToken(true)
                                                                 .build();

        dataStore.put(DUMMY_USERNAME, credentials);

        Assert.assertNotNull(dataStore.get(DUMMY_USERNAME));
        Assert.assertEquals(new DefaultCredentialsBuilder().withToken(new DefaultToken(DUMMY_TOKEN)).build(), dataStore.get(DUMMY_USERNAME));
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteWithNullKey() {
        dataStore.delete(null);
    }

    @Test
    public void testDeleteWithMissingKey() {
        Assert.assertNull(dataStore.delete("missing"));
    }

    @Test
    public void testDeleteWithExistingKey() {
        dataStore.put("testDeleteWithExistingKey", credentials);
        final Credentials deletedCredentials = dataStore.delete("testDeleteWithExistingKey");

        Assert.assertNotNull(credentials);
        Assert.assertEquals(credentials, deletedCredentials);
        Assert.assertNull(dataStore.delete("testDeleteWithExistingKey"));
    }
}
