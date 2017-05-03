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
package com.codenvy.api.user.server;

import com.codenvy.api.permission.server.SystemDomain;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.Path;

import static com.codenvy.api.user.server.UserServicePermissionsFilter.MANAGE_USERS_ACTION;

/**
 * Filter that covers calls to {@link AdminUserService} with authorization
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/admin/user{path:.*}")
public class AdminUserServicePermissionsFilter extends CheMethodInvokerFilter {
    @Override
    protected void filter(GenericResourceMethod grm, Object[] arguments) throws ApiException {
        final String methodName = grm.getMethod().getName();
        if ("getAll".equals(methodName) || "find".equals(methodName)) {
            EnvironmentContext.getCurrent().getSubject().checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION);
        } else {
            //unknown method
            throw new ForbiddenException("User is not authorized to perform this operation");
        }
    }
}
