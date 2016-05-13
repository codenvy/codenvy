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
package com.codenvy.api.permission.server;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

/**
 * Checks user's permission to perform some action with particular instance of given domain
 *
 * @author Sergii Leschenko
 */
public interface PermissionChecker {
    /**
     * @param user
     *         user id
     * @param domain
     *         domain id
     * @param instance
     *         instance id
     * @param action
     *         action name
     * @return true if the user has given permission
     * @throws NotFoundException
     *         when given domain is unsupported
     * @throws ServerException
     *         when any other error occurs during permission existence checking
     */
    boolean hasPermission(String user, String domain, String instance, String action) throws ServerException,
                                                                                             NotFoundException;
}
