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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * RedHat OAuth authentication
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 2/20/17.
 */
public class RedHatOAuthAuthenticator extends OAuthAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(RedHatOAuthAuthenticator.class);

    private final String userUri;

    @Inject
    public RedHatOAuthAuthenticator(@Nullable @Named("oauth.redhat.clientid") String clientId,
                                    @Nullable @Named("oauth.redhat.clientsecret") String clientSecret,
                                    @Nullable @Named("oauth.redhat.redirecturis") String[] redirectUris,
                                    @Nullable @Named("oauth.redhat.authuri") String authUri,
                                    @Nullable @Named("oauth.redhat.tokenuri") String tokenUri,
                                    @Nullable @Named("oauth.redhat.useruri") String userUri) throws IOException {
        if (!isNullOrEmpty(clientId)
            && !isNullOrEmpty(clientSecret)
            && !isNullOrEmpty(authUri)
            && !isNullOrEmpty(tokenUri)
            && !isNullOrEmpty(userUri)
            && redirectUris != null && redirectUris.length != 0) {
            configure(clientId, clientSecret, redirectUris, authUri, tokenUri, new MemoryDataStoreFactory());
        }
        this.userUri = userUri;
    }

    @Override
    public User getUser(OAuthToken accessToken) throws OAuthAuthenticationException {
        Map<String, String> params = new HashMap<>();
        params.put("Authorization", "Bearer " + accessToken.getToken());
        try {
            JsonValue userValue = doRequest(new URL(userUri), params);
            if (userValue.getElement("email") == null) {
                throw new OAuthAuthenticationException("Cannot retrieve user email, authentication impossible.");
            }
            final String email = userValue.getElement("email").getStringValue();
            User user = new RedHatUser();
            user.setEmail(email);
            String username = "";
            if (userValue.getElement("preferred_username") != null) {
                username = userValue.getElement("preferred_username").getStringValue().toLowerCase();
            } else if (userValue.getElement("given_name") != null && userValue.getElement("family_name") != null) {
                username = userValue.getElement("given_name").getStringValue().toLowerCase()
                                    .concat("_")
                                    .concat(userValue.getElement("family_name").getStringValue().toLowerCase());

            } else {
                username = email.substring(0, email.indexOf("@"));//
            }
            user.setName(username);
            return user;
        } catch (JsonParseException | IOException e) {
            throw new OAuthAuthenticationException(e.getMessage(), e);
        }
    }

    @Override
    public OAuthToken getToken(String userId) throws IOException {
        return super.getToken(userId);
    }

    @Override
    public final String getOAuthProvider() {
        return "redhat";
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
                LOG.warn("Can not receive RedHat user by path: {}. Response status: {}. Error message: {}",
                         tokenInfoUrl.toString(), responseCode, IoUtil
                                 .readStream(http.getErrorStream()));
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
