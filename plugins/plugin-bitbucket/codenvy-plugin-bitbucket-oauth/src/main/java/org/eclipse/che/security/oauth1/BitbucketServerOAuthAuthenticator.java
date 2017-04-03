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
package org.eclipse.che.security.oauth1;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * OAuth1 authentication for Bitbucket Server account.
 *
 * @author Igor Vinokur
 */
@Singleton
public class BitbucketServerOAuthAuthenticator extends OAuthAuthenticator {

    @Inject
    public BitbucketServerOAuthAuthenticator(@Named("oauth.bitbucket.consumerkey") String consumerKey,
                                             @Named("oauth.bitbucket.privatekey") String privateKey,
                                             @Named("bitbucket.endpoint") String bitbucketEndpoint,
                                             @Named("che.api") String apiEndpoint) {
        super(consumerKey,
              bitbucketEndpoint + "/plugins/servlet/oauth/request-token",
              bitbucketEndpoint + "/plugins/servlet/oauth/access-token",
              bitbucketEndpoint + "/plugins/servlet/oauth/authorize",
              apiEndpoint + "/oauth/1.0/callback",
              null,
              privateKey);

    }

    @Override
    public final String getOAuthProvider() {
        return "bitbucket-server";
    }
}
