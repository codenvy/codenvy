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
package com.codenvy.api.dao.authentication;
/**
 * @author Sergii Kabashniuk
 */

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holder of  AuthenticationHandlers.
 *
 * @author Sergii Kabashniuk
 */
public class AuthenticationHandlerProvider {

    private final Map<String, AuthenticationHandler> handlers;
    private final AuthenticationHandler              defaultHandler;

    @Inject
    public AuthenticationHandlerProvider(Set<AuthenticationHandler> handlers, @Named("sys.auth.handler.default") String defaultHandler) {
        this.handlers = new HashMap<>(handlers.size());
        for (AuthenticationHandler handler : handlers) {
            this.handlers.put(handler.getType(), handler);
        }
        this.defaultHandler = this.handlers.get(defaultHandler);
        if (this.defaultHandler == null) {
            throw new IllegalArgumentException(
                    "AuthenticationHandler with type " + defaultHandler + " is not found. And can't be default.");
        }
    }

    /**
     * Search handler by type.
     *
     * @param handlerType
     *         - given handler type.
     * @return -  AuthenticationHandler with given type.
     */
    public AuthenticationHandler getHandler(String handlerType) {
        return handlers.get(handlerType);
    }

    /**
     * @return AuthenticationHandler which handle request without type.
     */
    public AuthenticationHandler getDefaultHandler() {
        return defaultHandler;
    }

}
