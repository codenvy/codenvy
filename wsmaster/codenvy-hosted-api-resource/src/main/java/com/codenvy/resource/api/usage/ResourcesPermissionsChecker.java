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
package com.codenvy.resource.api.usage;

import org.eclipse.che.api.core.ForbiddenException;

/**
 * Allows to define different permissions checking for different accounts types.
 *
 * @author Sergii Leschenko
 */
public interface ResourcesPermissionsChecker {
    /**
     * Checks that current user is able to see resources information of specified account
     *
     * @param accountId
     *         account identifier
     * @throws ForbiddenException
     *         when current user doesn't have permission to see resources information
     */
    void checkResourcesVisibility(String accountId) throws ForbiddenException;

    /**
     * Returns account type for which this class checks permissions.
     */
    String getAccountType();
}
