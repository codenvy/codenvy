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
package com.codenvy.api.permission.shared.model;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * Represents users' permissions to access to some resources
 *
 * @author Sergii Leschenko
 */
public interface Permissions {
    /**
     * Returns user id
     *
     * <p>Note: also supported '*' for marking all users
     */
    String getUserId();

    /**
     * Returns domain id
     */
    String getDomainId();

    /**
     * Returns instance id. It is optional and can be null if domain supports it
     *
     * @see PermissionsDomain#isInstanceRequired()
     */
    @Nullable
    String getInstanceId();

    /**
     * List of actions which user can perform for particular instance
     */
    List<String> getActions();
}
