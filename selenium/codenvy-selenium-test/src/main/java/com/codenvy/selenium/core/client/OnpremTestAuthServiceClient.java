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
package com.codenvy.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class OnpremTestAuthServiceClient implements TestAuthServiceClient {
    private static final Logger LOG = LoggerFactory.getLogger(OnpremTestAuthServiceClient.class);

    private final String                 apiEndpoint;
    private final HttpJsonRequestFactory requestFactory;

    @Inject
    public OnpremTestAuthServiceClient(TestApiEndpointUrlProvider apiEndpointProvider,
                                       HttpJsonRequestFactory requestFactory) {
        this.apiEndpoint = apiEndpointProvider.get().toString();
        this.requestFactory = requestFactory;
    }

    @Override
    public String login(String username, String password) {
        StringBuilder jsonStringWithToken = new StringBuilder();
        BufferedReader br;
        String token = null;
        HttpURLConnection http = null;
        String line;
        try {
            String loginUrl = apiEndpoint + "/auth/login";
            http = (HttpURLConnection)new URL(loginUrl).openConnection();
            http.setRequestMethod("POST");
            http.setAllowUserInteraction(false);
            http.setRequestProperty("Content-Type", "application/json");
            http.setInstanceFollowRedirects(true);
            http.setDoOutput(true);
            OutputStream output = http.getOutputStream();
            output.write(("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}").getBytes("UTF-8"));
            if (http.getResponseCode() != 200) {
                throw new RuntimeException("Can not get token for user with login: '" + username + "' and password: '" + password +
                                           "' using the Codenvy REST API. Server response code: " + loginUrl + " " +
                                           http.getResponseCode() +
                                           IoUtil.readStream(http.getErrorStream()));
            }

            output.close();
            br = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
            while ((line = br.readLine()) != null) {
                jsonStringWithToken.append(line);
            }
            token = JsonHelper.parseJson(jsonStringWithToken.toString()).getElement("value").getStringValue();

        } catch (IOException | JsonParseException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
        return token;
    }

    /**
     * Logout from session by using authorization token
     *
     * @param authToken
     *         authorization token
     */
    @Override
    public void logout(String authToken) {
        try {
            String apiUrl = apiEndpoint + "/auth/logout?token=" + authToken;
            requestFactory.fromUrl(apiUrl)
                          .usePostMethod()
                          .request();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
