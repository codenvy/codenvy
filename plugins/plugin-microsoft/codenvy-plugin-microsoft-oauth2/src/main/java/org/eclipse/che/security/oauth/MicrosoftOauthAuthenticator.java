/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.security.oauth;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.security.oauth.shared.User;
import org.everrest.core.impl.provider.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth authentication for Microsoft account.
 *
 * @author Max Shaposhnik
 */
public class MicrosoftOauthAuthenticator extends OAuthAuthenticator {

  private static final Logger LOG = LoggerFactory.getLogger(MicrosoftOauthAuthenticator.class);

  private final String userUri;

  @Inject
  public MicrosoftOauthAuthenticator(
      @Nullable @Named("oauth.microsoft.clientid") String clientId,
      @Nullable @Named("oauth.microsoft.clientsecret") String clientSecret,
      @Nullable @Named("oauth.microsoft.redirecturis") String[] redirectUris,
      @Nullable @Named("oauth.microsoft.authuri") String authUri,
      @Nullable @Named("oauth.microsoft.tokenuri") String tokenUri,
      @Nullable @Named("oauth.microsoft.useruri") String userUri)
      throws IOException {
    if (!isNullOrEmpty(clientId)
        && !isNullOrEmpty(clientSecret)
        && !isNullOrEmpty(authUri)
        && !isNullOrEmpty(tokenUri)
        && !isNullOrEmpty(userUri)
        && redirectUris != null
        && redirectUris.length != 0) {

      configure(
          new MicrosoftAuthorizationCodeFlow.Builder(
                  BearerToken.authorizationHeaderAccessMethod(),
                  new NetHttpTransport(),
                  new JacksonFactory(),
                  new GenericUrl(tokenUri),
                  new MicrosoftParametersAuthentication(clientSecret),
                  clientId,
                  authUri)
              .setScopes(Arrays.asList("vso.code_manage", "vso.code_status"))
              .setDataStoreFactory(new MemoryDataStoreFactory())
              .build(),
          Arrays.asList(redirectUris));
    }

    this.userUri = userUri;
  }

  @Override
  public User getUser(OAuthToken accessToken) throws OAuthAuthenticationException {
    Map<String, String> params = new HashMap<>();
    params.put("Authorization", "Bearer " + accessToken.getToken());
    try {
      JsonValue userValue = doRequest(new URL(userUri), params);
      User user = new MicrosoftUser();
      user.setEmail(userValue.getElement("emailAddress").getStringValue());
      user.setName(userValue.getElement("displayName").getStringValue());
      return user;
    } catch (JsonParseException | IOException e) {
      throw new OAuthAuthenticationException(e.getMessage(), e);
    }
  }

  @Override
  protected String prepareState(URL requestUrl) {
    String state = super.prepareState(requestUrl);
    try {
      return URLEncoder.encode(state, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return state;
    }
  }

  @Override
  public final String getOAuthProvider() {
    return "microsoft";
  }

  private JsonValue doRequest(URL tokenInfoUrl, Map<String, String> params)
      throws IOException, JsonParseException {
    HttpURLConnection http = null;
    try {
      http = (HttpURLConnection) tokenInfoUrl.openConnection();
      http.setRequestMethod("GET");
      if (params != null) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
          http.setRequestProperty(entry.getKey(), entry.getValue());
        }
      }
      int responseCode = http.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        LOG.warn(
            "Can not receive microsoft user by path: {}. Response status: {}. Error message: {}",
            tokenInfoUrl.toString(),
            responseCode,
            IoUtil.readStream(http.getErrorStream()));
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
