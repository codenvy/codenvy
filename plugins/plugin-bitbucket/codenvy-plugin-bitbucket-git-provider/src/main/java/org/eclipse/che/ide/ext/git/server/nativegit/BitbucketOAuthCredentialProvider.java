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
package org.eclipse.che.ide.ext.git.server.nativegit;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.git.CredentialsProvider;
import org.eclipse.che.api.git.UserCredential;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.ProviderInfo;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.security.oauth.shared.OAuthTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
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
