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
package com.codenvy.ext.java.server;

import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Extends {@link DefaultHttpJsonRequestFactory} and aware about user's authorization token.
 * <p>Used for the purpose of sending authorized requests from WS-agent to the WS-master.
 *
 * @author Artem Zatsarynnyi
 * @see DefaultHttpJsonRequestFactory
 */
@Singleton
public class AuthorizeTokenHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {

    private final String userToken;

    @Inject
    public AuthorizeTokenHttpJsonRequestFactory(@Named("user.token") String token) {
        userToken = token;
    }

    @Override
    public HttpJsonRequest fromUrl(@NotNull String url) {
        return super.fromUrl(url).setAuthorizationHeader(userToken);
    }

    @Override
    public HttpJsonRequest fromLink(@NotNull Link link) {
        return super.fromLink(link).setAuthorizationHeader(userToken);
    }
}
