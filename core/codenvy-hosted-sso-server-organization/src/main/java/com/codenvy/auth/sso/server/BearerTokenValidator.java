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
package com.codenvy.auth.sso.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.model.impl.UserImpl;

import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;

import javax.inject.Inject;
import java.util.Map;

/**
 * Token validator implementation.
 */
public class BearerTokenValidator implements TokenValidator {

    @Inject
    private BearerTokenAuthenticationHandler handler;

    @Override
    public User validateToken(String token) throws ConflictException {
        Map<String, String> payload =  handler.getPayload(token);
        String username =  handler.getPayload(token).get("username");
        if (username == null || !handler.isValid(token))
            throw new ConflictException("Cannot create user - authentication token is invalid. Request a new one.");
        return new UserImpl(null, payload.get("email"), payload.get("username"));
    }
}
