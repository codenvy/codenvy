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
package com.codenvy.organization.api.permissions;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;

import java.util.Arrays;

/**
 * Domain for storing organizations' permissions
 *
 * @author Sergii Leschenko
 */
public class OrganizationDomain extends AbstractPermissionsDomain<MemberImpl> {
    public static final String DOMAIN_ID = "organization";

    public static final String UPDATE                  = "update";
    public static final String DELETE                  = "delete";
    public static final String MANAGE_SUBORGANIZATIONS = "manageSuborganizations";
    public static final String MANAGE_RESOURCES        = "manageResources";
    public static final String CREATE_WORKSPACES       = "createWorkspaces";
    public static final String MANAGE_WORKSPACES       = "manageWorkspaces";

    protected OrganizationDomain() {
        super(DOMAIN_ID, Arrays.asList(UPDATE,
                                       DELETE,
                                       MANAGE_SUBORGANIZATIONS,
                                       MANAGE_RESOURCES,
                                       CREATE_WORKSPACES,
                                       MANAGE_WORKSPACES));
    }
}
