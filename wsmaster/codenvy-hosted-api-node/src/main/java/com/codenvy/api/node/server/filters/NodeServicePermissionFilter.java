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
package com.codenvy.api.node.server.filters;

import com.codenvy.api.permission.server.SystemDomain;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.Path;

/**
 * Restricts access to methods of {@link NodeService} by users' permissions
 *
 * <p>Filter contains rules for protecting of all methods of {@link NodeService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Mihail Kuznyetsov
 */
@Filter
@Path("/nodes{path:(/.*)?}")
public class NodeServicePermissionFilter extends CheMethodInvokerFilter {
    @Override
    protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments) throws ApiException {

        final String methodName = genericMethodResource.getMethod().getName();
        switch (methodName) {
            case "getAddNodeScript":
            case "registerNode": {
                final Subject subject = EnvironmentContext.getCurrent().getSubject();
                subject.checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
                return;
            }
            default:
                throw new ForbiddenException("The user does not have permission to perform this operation");
        }
    }
}
