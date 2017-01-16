/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
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
