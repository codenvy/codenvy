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
package com.codenvy.resource.api;

import org.eclipse.che.api.core.ServerException;

/**
 * Returns key for fetching lock which will be used for locking resources
 * during resources operations for account with some type.
 *
 * @author Sergii Leschenko
 */
public interface ResourceLockKeyProvider {
    /**
     * Returns lock key by which resources should be lock during resources operations
     *
     * @param accountId
     *         account id
     * @return lock key by which resources should be lock during resources operations
     * @throws ServerException
     *         when any other exception occurs
     */
    String getLockKey(String accountId) throws ServerException;

    /**
     * Returns account type for which this class provides locks' ids
     */
    String getAccountType();
}
