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

import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.Data;

import java.io.IOException;
import java.util.Map;

/**
 * Allows to send client secret in "client_assertion" field.
 *
 * @author Max Shaposhnik
 */
public class MicrosoftParametersAuthentication implements
                                               HttpRequestInitializer,
                                               HttpExecuteInterceptor {

    private static final String CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    private static final String GRANT_TYPE            = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    /** Client secret or {@code null} for none. */
    private final String clientSecret;


    public MicrosoftParametersAuthentication(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public void intercept(HttpRequest request) throws IOException {
        Map<String, Object> data = Data.mapOf(UrlEncodedContent.getContent(request).getData());
        if (clientSecret != null) {
            data.put("client_assertion", clientSecret);
        }
        data.put("client_assertion_type", CLIENT_ASSERTION_TYPE);
        data.put("grant_type", GRANT_TYPE);
    }

    @Override
    public void initialize(HttpRequest request) throws IOException {
        request.setInterceptor(this);
    }
}
