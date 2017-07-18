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
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Used to store credentials when given url is WSO2.
 *
 * @author Eugene Voevodin
 */
@Singleton
public class WSO2OAuthCredentialsProvider implements CredentialsProvider {
    private static String OAUTH_PROVIDER_NAME = "wso2";
    public final Pattern WSO_2_URL_PATTERN;

    private static final Logger LOG = LoggerFactory.getLogger(WSO2OAuthCredentialsProvider.class);
    private final OAuthTokenProvider oAuthTokenProvider;

    @Inject
    public WSO2OAuthCredentialsProvider(OAuthTokenProvider oAuthTokenProvider,
                                        @Named("oauth.wso2.git.pattern") String gitPattern) {
        this.oAuthTokenProvider = oAuthTokenProvider;
        this.WSO_2_URL_PATTERN = Pattern.compile(gitPattern);
    }

    @Override
    public UserCredential getUserCredential() throws GitException {
        try {
            OAuthToken token = oAuthTokenProvider.getToken(OAUTH_PROVIDER_NAME, EnvironmentContext.getCurrent().getSubject().getUserId());
            if (token != null) {
                return new UserCredential(token.getToken(), "x-oauth-basic", OAUTH_PROVIDER_NAME);
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
        return WSO_2_URL_PATTERN.matcher(url).matches();
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return null;
    }


}
