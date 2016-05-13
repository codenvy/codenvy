/*
 *  [2012] - [2016] Codenvy, S.A.
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
package org.eclipse.che.ide.ext.git.server.nativegit;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.ProviderInfo;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.api.git.CredentialsProvider;
import org.eclipse.che.api.git.UserCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

/**
 * {@link CredentialsProvider} implementation for Bitbucket.
 *
 * @author Kevin Pollet
 */
@Singleton
public class BitbucketOAuthCredentialProvider implements CredentialsProvider {
    private static final Logger LOG                 = LoggerFactory.getLogger(BitbucketOAuthCredentialProvider.class);
    private static final String OAUTH_PROVIDER_NAME = "bitbucket";

    private final OAuthTokenProvider oAuthTokenProvider;

    @Inject
    public BitbucketOAuthCredentialProvider(@NotNull final OAuthTokenProvider oAuthTokenProvider) {
        this.oAuthTokenProvider = oAuthTokenProvider;
    }

    @Override
    public UserCredential getUserCredential() throws GitException {
        try {
            final OAuthToken token = oAuthTokenProvider.getToken(OAUTH_PROVIDER_NAME, EnvironmentContext.getCurrent().getSubject().getUserId());
            if (token != null) {
                return new UserCredential("x-token-auth", token.getToken(), OAUTH_PROVIDER_NAME);
            }

        } catch (IOException e) {
            LOG.warn(e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public String getId() {
        return OAUTH_PROVIDER_NAME;
    }

    @Override
    public boolean canProvideCredentials(String url) {
        return url.contains("bitbucket.org");
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return new ProviderInfo(OAUTH_PROVIDER_NAME, UriBuilder.fromUri("/oauth/authenticate")
                                                               .queryParam("oauth_provider", OAUTH_PROVIDER_NAME)
                                                               .queryParam("userId", EnvironmentContext.getCurrent().getSubject().getUserId())
                                                               .build()
                                                               .toString());
    }
}
