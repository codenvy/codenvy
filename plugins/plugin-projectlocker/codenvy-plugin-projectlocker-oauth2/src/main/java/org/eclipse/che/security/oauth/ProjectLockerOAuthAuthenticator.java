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
package org.eclipse.che.security.oauth;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.api.client.util.store.MemoryDataStoreFactory;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.security.oauth.shared.User;

/**
 * OAuth authentication for ProjectLocker account.
 *
 * @author Max Shaposhnik
 */
public class ProjectLockerOAuthAuthenticator extends OAuthAuthenticator {
  @Inject
  public ProjectLockerOAuthAuthenticator(
      @Nullable @Named("oauth.projectlocker.clientid") String clientId,
      @Nullable @Named("oauth.projectlocker.clientsecret") String clientSecret,
      @Nullable @Named("oauth.projectlocker.redirecturis") String[] redirectUris,
      @Nullable @Named("oauth.projectlocker.authuri") String authUri,
      @Nullable @Named("oauth.projectlocker.tokenuri") String tokenUri)
      throws IOException {
    if (!isNullOrEmpty(clientId)
        && !isNullOrEmpty(clientSecret)
        && !isNullOrEmpty(authUri)
        && !isNullOrEmpty(tokenUri)
        && redirectUris != null
        && redirectUris.length != 0) {

      configure(
          clientId, clientSecret, redirectUris, authUri, tokenUri, new MemoryDataStoreFactory());
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
