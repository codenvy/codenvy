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
