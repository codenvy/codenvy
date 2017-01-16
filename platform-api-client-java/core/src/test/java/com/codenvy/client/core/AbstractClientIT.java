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

import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.CredentialsProvider;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.core.auth.DefaultCredentialsBuilder;

import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * {@link com.codenvy.client.core.AbstractClient} tests.
 * 
 * @author Kevin Pollet
 */
public class AbstractClientIT extends AbstractIT {
    private final AuthenticationManager dummyAuthenticationManager;

    public AbstractClientIT() {
        super();
        final Credentials dummyCredentials = new DefaultCredentialsBuilder().withUsername(DUMMY_USERNAME)
                                                                      .withPassword(DUMMY_PASSWORD)
                                                                      .build();

        final CredentialsProvider dummyCredentialsProvider = mock(CredentialsProvider.class);

        this.dummyAuthenticationManager =
                                          new AuthenticationManager(REST_API_URL, DUMMY_USERNAME, dummyCredentials,
                                                                    dummyCredentialsProvider, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNewAbstractClientWithNullURL() {
        new AbstractClient(null, "dummyAPI", dummyAuthenticationManager) {
        };
    }

    @Test(expected = NullPointerException.class)
    public void testNewAbstractClientWithNullAPIName() {
        new AbstractClient(REST_API_URL, null, dummyAuthenticationManager) {
        };
    }

    @Test(expected = NullPointerException.class)
    public void testNewAbstractClientWithNullAuthenticationManager() {
        new AbstractClient(REST_API_URL, "dummyAPI", null) {
        };
    }
}
