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
