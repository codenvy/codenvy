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
