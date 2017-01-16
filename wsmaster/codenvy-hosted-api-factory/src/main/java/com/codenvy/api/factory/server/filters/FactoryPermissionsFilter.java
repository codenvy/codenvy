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
package com.codenvy.api.factory.server.filters;

import com.codenvy.api.workspace.server.WorkspaceDomain;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.Path;


/**
 * Restricts access to methods of FactoryService by user's permissions
 *
 * @author Anton Korneta
 */
@Filter
@Path("/factory/{path:.*}")
public class FactoryPermissionsFilter extends CheMethodInvokerFilter {

    @Override
    protected void filter(GenericResourceMethod genericResourceMethod, Object[] arguments) throws ApiException {
        final String methodName = genericResourceMethod.getMethod().getName();

        final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
        String action;
        String workspaceId;

        switch (methodName) {
            case "getFactoryJson": {
                workspaceId = ((String)arguments[0]);
                action = WorkspaceDomain.READ;
                break;
            }
            default:
                //public methods
                return;
        }
        currentSubject.checkPermission(WorkspaceDomain.DOMAIN_ID, workspaceId, action);
    }
}
