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
package com.codenvy.auth.sso.client;

import com.google.inject.ImplementedBy;

import org.eclipse.che.commons.subject.Subject;

/**
 * Provided communication bridge between sso client and server.
 *
 * @author Sergii Kabashniuk
 */
@ImplementedBy(HttpSsoServerClient.class)
public interface ServerClient {
    /**
     * Get subject associated with given token for the given execution context(workspaceId, accountId)
     *
     * @param token
     *         - sso authentication token.
     * @param clientUrl
     *         - url of client who asking the principal.
     * @return - principal with roles. If token is not valid return null.
     * @Deprecated use ServerClient.getUser(String token, String clientUrl)
     */

    Subject getSubject(String token, String clientUrl);
    /**
     * Notify server about termination sso session.
     *
     * @param token
     *         - sso authentication token.
     * @param clientUrl
     *         - url of client who asking the principal.
     */
    void unregisterClient(String token, String clientUrl);

}
