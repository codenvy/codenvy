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
package com.codenvy.api.permission.server.filter;

import com.codenvy.api.permission.server.InstanceParameterValidator;
import com.codenvy.api.permission.shared.dto.PermissionsDto;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.inject.Inject;
import javax.ws.rs.Path;

import static com.codenvy.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Restricts access to setting permissions of instance by users' setPermissions permission
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/permissions/")
public class SetPermissionsFilter extends CheMethodInvokerFilter {
    @Inject
    private InstanceParameterValidator instanceValidator;

    @Override
    public void filter(GenericResourceMethod genericResourceMethod, Object[] arguments)
            throws BadRequestException, ForbiddenException, NotFoundException, ServerException {
        final String methodName = genericResourceMethod.getMethod().getName();
        if (methodName.equals("storePermissions")) {
            final PermissionsDto permissions = (PermissionsDto)arguments[0];
            checkArgument(permissions != null, "Permissions descriptor required");
            checkArgument(!isNullOrEmpty(permissions.getDomainId()), "Domain required");
            instanceValidator.validate(permissions.getDomainId(), permissions.getInstanceId());

            if (!EnvironmentContext.getCurrent().getSubject().hasPermission(permissions.getDomainId(),
                                                                            permissions.getInstanceId(),
                                                                            SET_PERMISSIONS)) {
                throw new ForbiddenException("User can't edit permissions for this instance");
            }
        }
    }

    private void checkArgument(boolean expression, String message) throws BadRequestException {
        if (!expression) {
            throw new BadRequestException(message);
        }
    }
}
