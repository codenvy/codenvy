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

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

/**
 * Allows to parse "expires" field as a string (as MS server sends it).
 *
 * @author Max Shaposhnik
 */
public class MicrosoftTokenResponse extends GenericJson {
    /** Access token issued by the authorization server. */
    @Key("access_token")
    private String accessToken;

    /**
     * Token type (as specified in <a href="http://tools.ietf.org/html/rfc6749#section-7.1">Access
     * Token Types</a>).
     */
    @Key("token_type")
    private String tokenType;

    /**
     * Lifetime in seconds of the access token (for example 3600 for an hour) or {@code null} for
     * none.
     */
    @Key("expires_in")
    private String expiresInSeconds;

    /**
     * Refresh token which can be used to obtain new access tokens using {@link com.google.api.client.auth.oauth2.RefreshTokenRequest}
     * or {@code null} for none.
     */
    @Key("refresh_token")
    private String refreshToken;

    /**
     * Scope of the access token as specified in <a
     * href="http://tools.ietf.org/html/rfc6749#section-3.3">Access Token Scope</a> or {@code null}
     * for none.
     */
    @Key
    private String scope;

    /** Returns the access token issued by the authorization server. */
    public final String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token issued by the authorization server.
     * <p/>
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public MicrosoftTokenResponse setAccessToken(String accessToken) {
        this.accessToken = Preconditions.checkNotNull(accessToken);
        return this;
    }

    /**
     * Returns the token type (as specified in <a
     * href="http://tools.ietf.org/html/rfc6749#section-7.1">Access Token Types</a>).
     */
    public final String getTokenType() {
        return tokenType;
    }

    /**
     * Sets the token type (as specified in <a
     * href="http://tools.ietf.org/html/rfc6749#section-7.1">Access Token Types</a>).
     * <p/>
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public MicrosoftTokenResponse setTokenType(String tokenType) {
        this.tokenType = Preconditions.checkNotNull(tokenType);
        return this;
    }

    /**
     * Returns the lifetime in seconds of the access token (for example 3600 for an hour) or
     * {@code null} for none.
     */
    public final String getExpiresInSeconds() {
        return expiresInSeconds;
    }

    /**
     * Sets the lifetime in seconds of the access token (for example 3600 for an hour) or {@code null}
     * for none.
     * <p/>
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public MicrosoftTokenResponse setExpiresInSeconds(String expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
        return this;
    }

    /**
     * Returns the refresh token which can be used to obtain new access tokens using the same
     * authorization grant or {@code null} for none.
     */
    public final String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Sets the refresh token which can be used to obtain new access tokens using the same
     * authorization grant or {@code null} for none.
     * <p/>
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public MicrosoftTokenResponse setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    /**
     * Returns the scope of the access token or {@code null} for none.
     */
    public final String getScope() {
        return scope;
    }

    /**
     * Sets the scope of the access token or {@code null} for none.
     * <p/>
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public MicrosoftTokenResponse setScope(String scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public MicrosoftTokenResponse set(String fieldName, Object value) {
        return (MicrosoftTokenResponse)super.set(fieldName, value);
    }

    @Override
    public MicrosoftTokenResponse clone() {
        return (MicrosoftTokenResponse)super.clone();
    }
}
