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
package com.codenvy.api.user.server.dao;

import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;

/**
 * @author Anatoliy Bazko
 */
public interface AdminUserDao extends UserDao {

    /**
     * Gets all users from persistent layer.
     *
     * @param maxItems
     *         the maximum number of users to return
     * @param skipCount
     *         the number of users to skip
     * @return list of users POJO or empty list if no users were found
     * @throws IllegalArgumentException
     *         when {@code maxItems} or {@code skipCount} is negative
     * @throws ServerException
     *         when any other error occurs
     */
    Page<UserImpl> getAll(int maxItems, int skipCount) throws ServerException;
}
