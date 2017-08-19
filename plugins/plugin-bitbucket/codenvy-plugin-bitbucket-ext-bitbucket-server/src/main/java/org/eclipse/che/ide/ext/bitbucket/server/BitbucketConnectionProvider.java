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
package org.eclipse.che.ide.ext.bitbucket.server;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.security.oauth.shared.OAuthAuthorizationHeaderProvider;
import org.eclipse.che.security.oauth.shared.OAuthTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes connection to configured Bitbucket host.
 *
 * @author Igor Vinokur
 */
@Singleton
public class BitbucketConnectionProvider implements Provider<BitbucketConnection> {

  private static final Logger LOG = LoggerFactory.getLogger(BitbucketConnectionProvider.class);

  private final OAuthTokenProvider tokenProvider;
  private final OAuthAuthorizationHeaderProvider headerProvider;
  private final HttpJsonRequestFactory requestFactory;
  private final String apiEndpoint;

  @Inject
  BitbucketConnectionProvider(
      OAuthTokenProvider tokenProvider,
      OAuthAuthorizationHeaderProvider headerProvider,
      HttpJsonRequestFactory requestFactory,
      @Named("che.api") String apiEndpoint) {

    this.tokenProvider = tokenProvider;
    this.headerProvider = headerProvider;
    this.requestFactory = requestFactory;
    this.apiEndpoint = apiEndpoint;
  }

  @Override
  public BitbucketConnection get() {
    String endpoint = null;
    try {
      endpoint =
          requestFactory
              .fromUrl(apiEndpoint + "/bitbucket/endpoint")
              .useGetMethod()
              .request()
              .asString();
    } catch (Exception exception) {
      LOG.error(exception.getMessage());
    }

    return "https://bitbucket.org".equals(endpoint)
        ? new BitbucketConnectionImpl(tokenProvider)
        : new BitbucketServerConnectionImpl(endpoint, headerProvider);
  }
}
