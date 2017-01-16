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

import com.codenvy.client.Codenvy;
import com.codenvy.client.auth.Credentials;
import com.codenvy.client.core.auth.DefaultCredentialsBuilder;

import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Properties;


/**
 * REST client API base test.
 * 
 * @author Kevin Pollet
 */
public abstract class AbstractIT {
    private static final String REST_API_URL_PROPERTY_NAME = "rest.api.url";

    protected static final String DUMMY_USERNAME     = "dummyUsername";
    protected static final String DUMMY_PASSWORD     = "dummyPassword";
    protected static final String SDK_TOKEN_VALUE    = "123123";
    protected static final String SDK_WORKSPACE_NAME = "default";
    protected static Codenvy codenvy;
    protected static String  REST_API_URL;

    @BeforeClass
    public static void initRestAPIURL() {
        final Properties codenvySdkProperties = new Properties();
        try {
            codenvySdkProperties.load(AbstractIT.class.getResourceAsStream("/codenvy-sdk.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        REST_API_URL = System.getProperty(REST_API_URL_PROPERTY_NAME, codenvySdkProperties.getProperty(REST_API_URL_PROPERTY_NAME));
        Assert.assertNotNull(REST_API_URL);

        final Credentials credentials = new DefaultCredentialsBuilder().withUsername(DUMMY_USERNAME)
                                                                       .withPassword(DUMMY_PASSWORD)
                                                                       .build();

        codenvy = new DefaultCodenvyBuilder(REST_API_URL, DUMMY_USERNAME).withCredentials(credentials)
                                                                         .build();
    }
}
