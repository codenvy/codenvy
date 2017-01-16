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
