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
package com.codenvy.service.system;

import com.codenvy.api.permission.server.SystemDomain;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.system.server.SystemService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.Path;

/**
 * Rejects/allows access to the methods of {@link SystemService} & {@link HostedSystemService}.
 *
 * @author Yevhenii Voevodin
 */
@Filter
@Path("/system{path:.*}")
public class SystemServicePermissionsFilter extends CheMethodInvokerFilter {
    @Override
    protected void filter(GenericResourceMethod resource, Object[] args) throws ApiException {
        switch (resource.getMethod().getName()) {
            case "stop":
            case "getState":
                EnvironmentContext.getCurrent()
                                  .getSubject()
                                  .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
                break;
            case "getSystemRamLimitStatus":
                break;
            default:
                throw new ForbiddenException("The user does not have permission to perform this operation");
        }
    }
}
