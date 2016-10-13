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
package com.codenvy.api.audit.server;

import com.codenvy.api.permission.server.SystemDomain;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.Path;

import static com.codenvy.api.permission.server.SystemDomain.MANAGE_CODENVY_ACTION;

/**
 * Filter that covers calls to {@link AuditService} with authorization
 *
 * @author Igor Vinokur
 */
@Filter
@Path("/audit")
public class AuditServicePermissionsFilter extends CheMethodInvokerFilter {
    @Override
    protected void filter(GenericResourceMethod GenericResourceMethod, Object[] arguments) throws ApiException {
        String methodName = GenericResourceMethod.getMethod().getName();
        if ("downloadReport".equals(methodName)) {
            EnvironmentContext.getCurrent().getSubject().checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_CODENVY_ACTION);
        } else {
            //unknown method
            throw new ForbiddenException("User is not authorized to perform this operation");
        }
    }
}
