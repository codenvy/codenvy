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

import com.google.api.client.util.store.MemoryDataStoreFactory;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.security.oauth.shared.User;
import org.everrest.core.impl.provider.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;

/** OAuth authentication for wso2 account. */
@Singleton
public class WSO2OAuthAuthenticator extends OAuthAuthenticator {
    private static final Logger LOG = LoggerFactory.getLogger(WSO2OAuthAuthenticator.class);

    private static final String SCOPE = "openid";

    final String userUri;

    @Inject
    public WSO2OAuthAuthenticator(@Nullable @Named("oauth.wso2.clientid") String clientId,
                                  @Nullable @Named("oauth.wso2.clientsecret") String clientSecret,
                                  @Nullable @Named("oauth.wso2.redirecturis") String[] redirectUris,
                                  @Nullable @Named("oauth.wso2.authuri") String authUri,
                                  @Nullable @Named("oauth.wso2.tokenuri") String tokenUri,
                                  @Nullable @Named("oauth.wso2.useruri") String userUri) throws IOException {
        this.userUri = userUri;

        if (!isNullOrEmpty(clientId)
            && !isNullOrEmpty(clientSecret)
            && !isNullOrEmpty(authUri)
            && !isNullOrEmpty(tokenUri)
            && redirectUris != null && redirectUris.length != 0) {

            configure(clientId, clientSecret, redirectUris, authUri, tokenUri, new MemoryDataStoreFactory(), singletonList(SCOPE));
        }
    }

    /** {@inheritDoc} */
    @Override
    public User getUser(OAuthToken accessToken) throws OAuthAuthenticationException {
        URL getUserUrL;
        Map<String, String> params = new HashMap<>();
        params.put("Authorization", "Bearer " + accessToken.getToken());
        try {
            getUserUrL = new URL(String.format("%s?schema=%s", userUri, SCOPE));
            JsonValue userValue = doRequest(getUserUrL, params);
            User user = new Wso2User();
            user.setEmail(userValue.getElement("email").getStringValue());
            user.setName(userValue.getElement("name").getStringValue());
            return user;
        } catch (JsonParseException | IOException e) {
            throw new OAuthAuthenticationException(e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getOAuthProvider() {
        return "wso2";
    }

    @Override
    public OAuthToken getToken(String userId) throws IOException {
        final OAuthToken token = super.getToken(userId);
        if (token != null) {
            token.setScope(SCOPE);
        }
        return token;
    }

    private JsonValue doRequest(URL tokenInfoUrl, Map<String, String> params) throws IOException, JsonParseException {
        HttpURLConnection http = null;
        try {
            http = (HttpURLConnection)tokenInfoUrl.openConnection();
            http.setRequestMethod("GET");
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    http.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            int responseCode = http.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOG.warn("Can not receive wso2 token by path: {}. Response status: {}. Error message: {}",
                         tokenInfoUrl.toString(), responseCode, IoUtil.readStream(http.getErrorStream()));
                return null;
            }

            JsonValue result;
            try (InputStream input = http.getInputStream()) {
                result = JsonHelper.parseJson(input);
            }
            return result;
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
    }
}
