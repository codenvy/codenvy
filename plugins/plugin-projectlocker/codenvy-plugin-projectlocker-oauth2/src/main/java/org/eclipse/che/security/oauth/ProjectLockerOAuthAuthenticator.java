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
package org.eclipse.che.security.oauth;

import com.google.api.client.util.store.MemoryDataStoreFactory;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.security.oauth.shared.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * OAuth authentication for ProjectLocker account.
 *
 * @author Max Shaposhnik
 */
public class ProjectLockerOAuthAuthenticator extends OAuthAuthenticator {
    @Inject
    public ProjectLockerOAuthAuthenticator(@Nullable @Named("oauth.projectlocker.clientid") String clientId,
                                           @Nullable @Named("oauth.projectlocker.clientsecret") String clientSecret,
                                           @Nullable @Named("oauth.projectlocker.redirecturis") String[] redirectUris,
                                           @Nullable @Named("oauth.projectlocker.authuri") String authUri,
                                           @Nullable @Named("oauth.projectlocker.tokenuri") String tokenUri) throws IOException {
        if (!isNullOrEmpty(clientId)
            && !isNullOrEmpty(clientSecret)
            && !isNullOrEmpty(authUri)
            && !isNullOrEmpty(tokenUri)
            && redirectUris != null && redirectUris.length != 0) {

            configure(clientId, clientSecret, redirectUris, authUri, tokenUri, new MemoryDataStoreFactory());
        }
    }

    @Override
    public User getUser(OAuthToken accessToken) throws OAuthAuthenticationException {
        return null;
    }

    @Override
    public OAuthToken getToken(String userId) throws IOException {
        return super.getToken(userId);
    }

    @Override
    public final String getOAuthProvider() {
        return "projectlocker";
    }
}
