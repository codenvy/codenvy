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
package com.codenvy.api.workspace.server.filters;

import com.codenvy.api.permission.shared.dto.PermissionsDto;
import com.codenvy.api.workspace.server.stack.StackDomain;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.Path;

import static com.codenvy.api.machine.server.recipe.RecipeDomain.DOMAIN_ID;
import static com.codenvy.api.machine.server.recipe.RecipeDomain.READ;

/**
 * Restricts access to setting public permissions for stacks and recipes
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/permissions/")
public class SetPublicPermissionsFilter extends CheMethodInvokerFilter {
    @Override
    public void filter(GenericResourceMethod genericResourceMethod, Object[] arguments) throws UnauthorizedException,
                                                                                               ForbiddenException,
                                                                                               ServerException {
        final String methodName = genericResourceMethod.getMethod().getName();
        if (methodName.equals("storePermissions")) {
            final PermissionsDto permissions = (PermissionsDto)arguments[0];

            final String domain = permissions.getDomainId();

            boolean recipeDomain;
            boolean stackDomain = false;
            if (!(recipeDomain = DOMAIN_ID.equals(domain))
                && !(stackDomain = StackDomain.DOMAIN_ID.equals(domain))) {

                //process only recipes' and stacks' permissions
                return;
            }

            if (permissions.getUserId().equals("*")
                && ((permissions.getActions().size() != 1)
                    || recipeDomain && !permissions.getActions().contains(READ)
                    || stackDomain && !permissions.getActions().contains(StackDomain.READ))) {
                throw new ForbiddenException("Public permissions support only 'read' action");
            }
        }
    }
}
