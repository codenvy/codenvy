/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.security.oauth;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

/**
 * Flow implementation for Microsoft requests, use special request and response types.
 *
 * @author Max Shaposhnik
 */
public class MicrosoftAuthorizationCodeFlow extends AuthorizationCodeFlow {

    /**
     * @param builder
     *         Microsoft authorization code flow builder
     */
    protected MicrosoftAuthorizationCodeFlow(Builder builder) {
        super(builder);
    }

    @Override
    public AuthorizationCodeTokenRequest newTokenRequest(String authorizationCode) {
        // don't need to specify clientId
        // don't need to specify redirectUri to give control of it to user of this class
        return new MicrosoftAuthorizationCodeTokenRequest(getTransport(),
                                                          getJsonFactory(),
                                                          getTokenServerEncodedUrl(), "", authorizationCode, "").setClientAuthentication(
                getClientAuthentication())
                                                                                                                .setRequestInitializer(
                                                                                                                        getRequestInitializer())
                                                                                                                .setScopes(getScopes());
    }


    /**
     * Microsoft authorization code flow builder.
     */
    public static class Builder extends AuthorizationCodeFlow.Builder {

        public Builder(Credential.AccessMethod method,
                       HttpTransport transport,
                       JsonFactory jsonFactory,
                       GenericUrl tokenServerUrl,
                       HttpExecuteInterceptor clientAuthentication,
                       String clientId,
                       String authorizationServerEncodedUrl) {
            super(method, transport, jsonFactory, tokenServerUrl, clientAuthentication, clientId, authorizationServerEncodedUrl);
        }

        @Override
        public MicrosoftAuthorizationCodeFlow build() {
            return new MicrosoftAuthorizationCodeFlow(this);
        }

    }
}
