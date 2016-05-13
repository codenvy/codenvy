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
package com.codenvy.api.permission.server.filter;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericMethodResource;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Restricts access to reading permissions of instance by users' readPermissions permission
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/permissions/{domain}/{instance}/list")
public class GetPermissionsFilter extends CheMethodInvokerFilter {
    @PathParam("domain")
    private String domain;

    @PathParam("instance")
    private String instance;

    @Override
    public void filter(GenericMethodResource genericMethodResource, Object[] arguments)
            throws UnauthorizedException, ForbiddenException, ServerException {
        final String methodName = genericMethodResource.getMethod().getName();
        if (methodName.equals("getUsersPermissionsByInstance")) {
            if (!EnvironmentContext.getCurrent().getSubject().hasPermission(domain, instance, "readPermissions")) {
                throw new ForbiddenException("User can't get list of permissions for this instance");
            }
        }
    }
}
