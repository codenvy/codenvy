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
package com.codenvy.api.permission.server.account;

import org.eclipse.che.api.core.ForbiddenException;

/**
 * Defines permissions checking for accounts with some type.
 *
 * @author Sergii Leshchenko
 */
public interface AccountPermissionsChecker {
    /**
     * Checks that current subject is authorized to perform
     * given operation with specified account
     *
     * @param accountId
     *         account to check
     * @param operation
     *         operation that is going to be performed
     * @throws ForbiddenException
     *         when user doesn't have permissions to perform specified operation
     */
    void checkPermissions(String accountId, AccountOperation operation) throws ForbiddenException;

    /**
     * Returns account type for which this class tracks check resources permissions.
     */
    String getAccountType();
}
