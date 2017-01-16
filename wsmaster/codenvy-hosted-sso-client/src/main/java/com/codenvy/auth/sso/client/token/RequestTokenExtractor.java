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
package com.codenvy.auth.sso.client.token;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/** Allow extract sso token from request. */
public interface RequestTokenExtractor {
    /**
     * Extract token from request.
     *
     * @param req
     *         - request object.
     * @return - token if it was found, null otherwise.
     */
    String getToken(HttpServletRequest req);
}
