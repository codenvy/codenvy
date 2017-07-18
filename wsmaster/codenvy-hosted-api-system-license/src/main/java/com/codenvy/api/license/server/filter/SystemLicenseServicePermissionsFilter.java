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
package com.codenvy.api.license.server.filter;

import com.codenvy.api.license.server.SystemLicenseService;
import com.codenvy.api.permission.server.SystemDomain;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.Path;

import static com.codenvy.api.permission.server.SystemDomain.MANAGE_SYSTEM_ACTION;

/**
 * Filter that covers calls to {@link SystemLicenseService} with authorization
 *
 * @author Sergii Leschenko
 * @author Alexander Andrienko
 */
@Filter
@Path("/license/system{path:.*}")
public class SystemLicenseServicePermissionsFilter extends CheMethodInvokerFilter {
    @Override
    protected void filter(GenericResourceMethod GenericResourceMethod, Object[] arguments) throws ApiException {
        String methodName = GenericResourceMethod.getMethod().getName();
        if (methodName.equals("isSystemUsageLegal")) {
            //public method
            return;
        }

        EnvironmentContext.getCurrent().getSubject().checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION);
    }
}
