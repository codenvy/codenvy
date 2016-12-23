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
package com.codenvy.api.license;

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
                    throw new ForbiddenException(licenseManager.getMessageForLicenseCompletelyExpired());
                }

                break;

            default:
                break;
        }
    }

}
