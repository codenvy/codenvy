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
 * OAuth authentication via LinkedIn.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
public class LinkedInOAuthAuthenticator extends OAuthAuthenticator {


    private static final Logger LOG = LoggerFactory.getLogger(LinkedInOAuthAuthenticator.class);

    private final String userUri;

    @Inject
    public LinkedInOAuthAuthenticator(@Nullable @Named("oauth.linkedin.clientid") String clientId,
                                      @Nullable @Named("oauth.linkedin.clientsecret") String clientSecret,
                                      @Nullable @Named("oauth.linkedin.redirecturis") String[] redirectUris,
                                      @Nullable @Named("oauth.linkedin.authuri") String authUri,
                                      @Nullable @Named("oauth.linkedin.tokenuri") String tokenUri,
                                      @Nullable @Named("oauth.linkedin.useruri") String userUri) throws IOException {
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
            // LinkedIn specific trick (default user URL does not returns email)
            final String values = "(email-address,first-name,last-name)";
            final String query = String.format("format=json");
            JsonValue userValue = doRequest(new URL(userUri + ":" + values + "?" + query), params);
            if (userValue.getElement("emailAddress")  == null) {
                throw new OAuthAuthenticationException("Cannot retrieve user email, authentication impossible.");
            }
            final String email =  userValue.getElement("emailAddress").getStringValue();
            String username = "";
            User user = new LinkedInUser();
            user.setEmail(email);
            if (userValue.getElement("firstName") == null || userValue.getElement("lastName") == null) {
                username = email.substring(0, email.indexOf("@"));
            } else {
                username = userValue.getElement("firstName").getStringValue().toLowerCase()
                                  .concat("_")
                                  .concat(userValue.getElement("lastName").getStringValue().toLowerCase());
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
        return "linkedin";
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
                LOG.warn("Can not receive LinkedIn user by path: {}. Response status: {}. Error message: {}",
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
