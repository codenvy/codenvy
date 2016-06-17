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
package com.codenvy.api.workspace.server.filters;

import com.codenvy.api.permission.shared.dto.PermissionsDto;
import com.codenvy.api.workspace.server.recipe.RecipeDomain;
import com.codenvy.api.workspace.server.stack.StackDomain;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericMethodResource;

import javax.ws.rs.Path;

/**
 * Restricts access to setting public permissions for stacks and recipes
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/permissions/")
public class AclSetPermissionsFilter extends CheMethodInvokerFilter {
    @Override
    public void filter(GenericMethodResource genericMethodResource, Object[] arguments)
            throws UnauthorizedException, ForbiddenException, ServerException {
        final String methodName = genericMethodResource.getMethod().getName();
        if (methodName.equals("storePermissions")) {
            final PermissionsDto permissions = (PermissionsDto)arguments[0];

            final String domain = permissions.getDomain();

            if (!RecipeDomain.DOMAIN_ID.equals(domain) && !StackDomain.DOMAIN_ID.equals(domain)) {
                //process only recipes' and stacks' permissions
                return;
            }

            if (permissions.getUser().equals("*")
                && (permissions.getActions().size() != 1
                    || !permissions.getActions().contains(RecipeDomain.READ))) {
                throw new ForbiddenException("Public permissions support only 'read' action");
            }
        }
    }
}
