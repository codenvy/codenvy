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

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.util.Collection;

/**
 * Microsoft implementation of token request. Sends auth code in "assertion" field,
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
public class MicrosoftAuthorizationCodeTokenRequest extends AuthorizationCodeTokenRequest {


    @Key
    private String assertion;

    /**
     * @param transport
     *         HTTP transport
     * @param jsonFactory
     *         JSON factory
     * @param tokenServerUrl
     *         token server URL
     * @param code
     *         authorization code generated by the authorization server
     */
    public MicrosoftAuthorizationCodeTokenRequest(HttpTransport transport,
                                                  JsonFactory jsonFactory,
                                                  GenericUrl tokenServerUrl, String code) {
        super(transport, jsonFactory, tokenServerUrl, null);
        this.assertion = code;
    }

    /**
     * @param transport
     *         HTTP transport
     * @param jsonFactory
     *         JSON factory
     * @param tokenServerEncodedUrl
     *         token server encoded URL
     * @param clientSecret
     *         client secret
     * @param code
     *         authorization code generated by the authorization server
     * @param redirectUri
     *         redirect URL parameter matching the redirect URL parameter in the
     *         authorization request (see {@link #setRedirectUri(String)}
     */
    public MicrosoftAuthorizationCodeTokenRequest(HttpTransport transport, JsonFactory jsonFactory,
                                                  String tokenServerEncodedUrl, String clientSecret, String code,
                                                  String redirectUri) {
        super(transport, jsonFactory, new GenericUrl(tokenServerEncodedUrl), code);
        this.assertion = code;
        setClientAuthentication(new MicrosoftParametersAuthentication(clientSecret));
        setRedirectUri(redirectUri);
    }


    @Override
    public MicrosoftAuthorizationCodeTokenRequest setRequestInitializer(
            HttpRequestInitializer requestInitializer) {
        return (MicrosoftAuthorizationCodeTokenRequest)super.setRequestInitializer(requestInitializer);
    }

    @Override
    public MicrosoftAuthorizationCodeTokenRequest setTokenServerUrl(GenericUrl tokenServerUrl) {
        return (MicrosoftAuthorizationCodeTokenRequest)super.setTokenServerUrl(tokenServerUrl);
    }

    @Override
    public MicrosoftAuthorizationCodeTokenRequest setScopes(Collection<String> scopes) {
        return (MicrosoftAuthorizationCodeTokenRequest)super.setScopes(scopes);
    }

    @Override
    public MicrosoftAuthorizationCodeTokenRequest setGrantType(String grantType) {
        return (MicrosoftAuthorizationCodeTokenRequest)super.setGrantType(grantType);
    }

    @Override
    public MicrosoftAuthorizationCodeTokenRequest setClientAuthentication(
            HttpExecuteInterceptor clientAuthentication) {
        Preconditions.checkNotNull(clientAuthentication);
        return (MicrosoftAuthorizationCodeTokenRequest)super.setClientAuthentication(
                clientAuthentication);
    }

    @Override
    public MicrosoftAuthorizationCodeTokenRequest setCode(String code) {
        this.assertion = Preconditions.checkNotNull(code);
        return this;
    }

    @Override
    public MicrosoftAuthorizationCodeTokenRequest setRedirectUri(String redirectUri) {
        Preconditions.checkNotNull(redirectUri);
        return (MicrosoftAuthorizationCodeTokenRequest)super.setRedirectUri(redirectUri);
    }

    @Override
    public MicrosoftAuthorizationCodeTokenRequest set(String fieldName, Object value) {
        return (MicrosoftAuthorizationCodeTokenRequest)super.set(fieldName, value);
    }

    @Override
    public TokenResponse execute() throws IOException {
        MicrosoftTokenResponse response = executeUnparsed().parseAs(MicrosoftTokenResponse.class);
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(response.getAccessToken())
                     .setScope(response.getScope())
                     .setRefreshToken(response.getRefreshToken())
                     .setExpiresInSeconds(Long.parseLong(response.getExpiresInSeconds()));
        return tokenResponse;
    }
}
