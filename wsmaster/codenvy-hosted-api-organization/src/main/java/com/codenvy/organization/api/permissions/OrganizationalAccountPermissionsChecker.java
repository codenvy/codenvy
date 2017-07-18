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
package com.codenvy.organization.api.permissions;

import com.codenvy.api.permission.server.account.AccountOperation;
import com.codenvy.api.permission.server.account.AccountPermissionsChecker;
import com.codenvy.organization.spi.impl.OrganizationImpl;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

import javax.inject.Singleton;

import static com.codenvy.organization.api.permissions.OrganizationDomain.CREATE_WORKSPACES;
import static com.codenvy.organization.api.permissions.OrganizationDomain.DOMAIN_ID;
import static com.codenvy.organization.api.permissions.OrganizationDomain.MANAGE_RESOURCES;
import static com.codenvy.organization.api.permissions.OrganizationDomain.MANAGE_WORKSPACES;

/**
 * Defines permissions checking for organizational accounts.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class OrganizationalAccountPermissionsChecker implements AccountPermissionsChecker {
    @Override
    public void checkPermissions(String accountId, AccountOperation operation) throws ForbiddenException {
        Subject subject = EnvironmentContext.getCurrent().getSubject();
        switch (operation) {
            case CREATE_WORKSPACE:
                if (!subject.hasPermission(OrganizationDomain.DOMAIN_ID, accountId, OrganizationDomain.CREATE_WORKSPACES)) {
                    throw new ForbiddenException("User is not authorized to create workspaces in specified namespace.");
                }
                break;
            case MANAGE_WORKSPACES:
                if (!subject.hasPermission(OrganizationDomain.DOMAIN_ID, accountId, OrganizationDomain.MANAGE_WORKSPACES)) {
                    throw new ForbiddenException("User is not authorized to use specified namespace.");
                }
                break;
            case SEE_RESOURCE_INFORMATION:
                if (subject.hasPermission(DOMAIN_ID, accountId, CREATE_WORKSPACES)
                    || subject.hasPermission(DOMAIN_ID, accountId, MANAGE_WORKSPACES)
                    || subject.hasPermission(DOMAIN_ID, accountId, MANAGE_RESOURCES)) {

                    // user is able to see resources usage information
                    return;
                }
                throw new ForbiddenException("User is not authorized to see resources information of requested organization.");
            default:
                throw new ForbiddenException("User is not authorized to use specified namespace.");
        }
    }

    @Override
    public String getAccountType() {
        return OrganizationImpl.ORGANIZATIONAL_ACCOUNT;
    }
}
