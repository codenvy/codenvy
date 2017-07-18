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
