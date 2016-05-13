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

import com.codenvy.api.permission.shared.dto.PermissionsDto;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericMethodResource;

import javax.ws.rs.Path;

/**
 * Restricts access to setting permissions of instance by users' setPermissions permission
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/permissions/")
public class SetPermissionsFilter extends CheMethodInvokerFilter {
    @Override
    public void filter(GenericMethodResource genericMethodResource, Object[] arguments)
            throws UnauthorizedException, ForbiddenException, ServerException {
        final String methodName = genericMethodResource.getMethod().getName();
        if (methodName.equals("storePermissions")) {
            final PermissionsDto permissions = (PermissionsDto)arguments[0];

            if (!EnvironmentContext.getCurrent().getSubject().hasPermission(permissions.getDomain(),
                                                                         permissions.getInstance(),
                                                                         "setPermissions")) {
                throw new ForbiddenException("User can't edit permissions for this instance");
            }

            // User doesn't have ability to add public permissions by REST API
            // For now it can be added only by admins by predefined stacks and recipes
            if (permissions.getUser().equals("*")) {
                throw new ForbiddenException("User can't set public permissions for this instance");
            }
        }
    }
}
