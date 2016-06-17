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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;

/**
 * Implementation of {@link PermissionChecker} that use {@link PermissionManager} for checking
 *
 * @author Sergii Leschenko
 */
public class PermissionCheckerImpl implements PermissionChecker {
    private final PermissionManager permissionManager;

    @Inject
    public PermissionCheckerImpl(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean hasPermission(String user, String domain, String instance, String action) throws ServerException,
                                                                                                    NotFoundException,
                                                                                                    ConflictException {
        return permissionManager.exists(user, domain, instance, action);
    }
}
