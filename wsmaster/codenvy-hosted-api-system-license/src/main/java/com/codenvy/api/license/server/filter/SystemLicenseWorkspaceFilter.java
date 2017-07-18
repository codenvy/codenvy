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

import com.codenvy.api.license.server.SystemLicenseManager;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.inject.Inject;
import javax.ws.rs.Path;

/**
 * Checks system license condition for workspace usage.
 *
 * @author Dmytro Nochevnov
 */
@Filter
@Path("/workspace{path:(/.*)?}")
public class SystemLicenseWorkspaceFilter extends CheMethodInvokerFilter {
    @Inject
    protected SystemLicenseManager licenseManager;

    @Override
    public void filter(GenericResourceMethod genericResourceMethod, Object[] arguments) throws ServerException, ForbiddenException {
        final String methodName = genericResourceMethod.getMethod().getName();

        switch (methodName) {
            case "startFromConfig":
            case "startById":
                if (!licenseManager.canStartWorkspace()) {
                    throw new ForbiddenException(licenseManager.getMessageWhenUserCannotStartWorkspace());
                }

                break;

            default:
                break;
        }
    }

}
