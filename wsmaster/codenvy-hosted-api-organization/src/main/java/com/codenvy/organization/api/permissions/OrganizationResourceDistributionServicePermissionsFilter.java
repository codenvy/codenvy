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
package com.codenvy.organization.api.permissions;

import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.api.resource.OrganizationResourcesDistributionService;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.inject.Inject;
import javax.ws.rs.Path;

/**
 * Restricts access to methods of {@link OrganizationResourcesDistributionService} by users' permissions.
 *
 * <p>Filter contains rules for protecting of all methods of {@link OrganizationResourcesDistributionService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}.
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/organization/resource{path:(/.*)?}")
public class OrganizationResourceDistributionServicePermissionsFilter extends CheMethodInvokerFilter {
    static final String DISTRIBUTE_RESOURCES_METHOD      = "distribute";
    static final String GET_DISTRIBUTED_RESOURCES_METHOD = "getDistributedResources";
    static final String RESET_DISTRIBUTED_RESOURCES      = "reset";

    @Inject
    private OrganizationManager organizationManager;

    @Override
    protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments) throws ApiException {
        final String methodName = genericMethodResource.getMethod().getName();

        final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
        String organizationId;
        switch (methodName) {
            case DISTRIBUTE_RESOURCES_METHOD:
            case RESET_DISTRIBUTED_RESOURCES:
                //we should check permissions on parent organization level
                organizationId = organizationManager.getById((String)arguments[0]).getParent();
                if (organizationId == null) {
                    // requested organization is root so manager should throw exception
                    return;
                }
                break;

            case GET_DISTRIBUTED_RESOURCES_METHOD:
                organizationId = (String)arguments[0];
                // get organization to ensure that organization exists
                organizationManager.getById(organizationId);
                if (currentSubject.hasPermission(SystemDomain.DOMAIN_ID, null, OrganizationPermissionsFilter.MANAGE_ORGANIZATIONS_ACTION)) {
                    //user is able to see information about all organizations
                    return;
                }
                break;

            default:
                throw new ForbiddenException("The user does not have permission to perform this operation");
        }

        if (!currentSubject.hasPermission(OrganizationDomain.DOMAIN_ID, organizationId, OrganizationDomain.MANAGE_RESOURCES)) {
            throw new ForbiddenException("The user does not have permission to manage resources of organization with id '"
                                         + organizationId + "'");
        }
    }
}
