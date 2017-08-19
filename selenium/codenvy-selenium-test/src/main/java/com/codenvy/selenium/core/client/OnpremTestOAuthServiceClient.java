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
package com.codenvy.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;

/** @author Andrey Chizhikov */
@Singleton
public class OnpremTestOAuthServiceClient {
  private final String apiEndpoint;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public OnpremTestOAuthServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider, HttpJsonRequestFactory requestFactory) {
    this.apiEndpoint = apiEndpointProvider.get().toString();
    this.requestFactory = requestFactory;
  }

  /**
   * Invalidate OAuth provider
   *
   * @param provider the name of OAuth provider
   */
  public synchronized void invalidateOAuthProvider(String provider, String token) throws Exception {
    requestFactory
        .fromUrl(apiEndpoint + "oauth/token?oauth_provider=" + provider)
        .useDeleteMethod()
        .setAuthorizationHeader(token)
        .request();
  }

  /**
   * Check if OAuth provider is valid.
   *
   * @param provider the name of OAuth provider
   * @return {@code true} if OAuth provider is founded
   */
  public boolean isOAuthProviderValid(String provider, String token) {
    try {
      requestFactory
          .fromUrl(apiEndpoint + "oauth/token?oauth_provider=" + provider)
          .useGetMethod()
          .setAuthorizationHeader(token)
          .request();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
