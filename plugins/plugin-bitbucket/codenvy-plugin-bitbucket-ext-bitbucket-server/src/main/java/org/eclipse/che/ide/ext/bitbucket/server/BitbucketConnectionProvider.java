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
package org.eclipse.che.ide.ext.bitbucket.server;

import org.eclipse.che.api.auth.oauth.OAuthAuthorizationHeaderProvider;
import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Initializes connection to configured Bitbucket host.
 *
 * @author Igor Vinokur
 */
@Singleton
public class BitbucketConnectionProvider implements Provider<BitbucketConnection> {

    private static final Logger LOG = LoggerFactory.getLogger(BitbucketConnectionProvider.class);

    private final OAuthTokenProvider               tokenProvider;
    private final OAuthAuthorizationHeaderProvider headerProvider;
    private final HttpJsonRequestFactory           requestFactory;
    private final String                           apiEndpoint;

    @Inject
    BitbucketConnectionProvider(OAuthTokenProvider tokenProvider,
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
            endpoint = requestFactory.fromUrl(apiEndpoint + "/bitbucket/endpoint")
                                     .useGetMethod()
                                     .request()
                                     .asString();
        } catch (Exception exception) {
            LOG.error(exception.getMessage());
        }

        return "https://bitbucket.org".equals(endpoint) ? new BitbucketConnectionImpl(tokenProvider)
                                                        : new BitbucketServerConnectionImpl(endpoint, headerProvider);
    }
}
