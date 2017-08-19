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
public class MicrosoftParametersAuthentication
    implements HttpRequestInitializer, HttpExecuteInterceptor {

  private static final String CLIENT_ASSERTION_TYPE =
      "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
  private static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";

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
