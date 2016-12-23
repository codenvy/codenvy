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
import com.codenvy.organization.api.OrganizationService;
import com.codenvy.organization.shared.dto.OrganizationDto;
import com.codenvy.organization.shared.model.Organization;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.inject.Inject;
import javax.ws.rs.Path;

import static com.codenvy.organization.api.permissions.OrganizationDomain.DOMAIN_ID;
import static com.codenvy.organization.api.permissions.OrganizationDomain.MANAGE_SUBORGANIZATIONS;

/**
 * Restricts access to methods of {@link OrganizationService} by users' permissions
 *
 * <p>Filter contains rules for protecting of all methods of {@link OrganizationService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/organization{path:(?!/resource)(/.*)?}")
public class OrganizationPermissionsFilter extends CheMethodInvokerFilter {
    // action of system domain for granting admins ability to work with foreign organizations
    public static final String MANAGE_ORGANIZATIONS_ACTION = "manageOrganizations";

    static final String CREATE_METHOD            = "create";
    static final String UPDATE_METHOD            = "update";
    static final String REMOVE_METHOD            = "remove";
    static final String GET_BY_PARENT_METHOD     = "getByParent";
    static final String GET_ORGANIZATIONS_METHOD = "getOrganizations";
    static final String GET_BY_ID_METHOD         = "getById";
    static final String FIND_METHOD              = "find";

    @Inject
    private OrganizationManager manager;

    @Override
    protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments) throws ApiException {
        final String methodName = genericMethodResource.getMethod().getName();

        final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
        String action;
        String organizationId;

        switch (methodName) {
            case CREATE_METHOD:
                final OrganizationDto organization = (OrganizationDto)arguments[0];
                if (organization.getParent() != null) {
                    organizationId = organization.getParent();
                    action = OrganizationDomain.MANAGE_SUBORGANIZATIONS;
                    break;
                }
                //anybody can create root organization
                return;

            case UPDATE_METHOD:
                organizationId = ((String)arguments[0]);
                action = OrganizationDomain.UPDATE;
                break;

            case REMOVE_METHOD:
                organizationId = ((String)arguments[0]);
                action = OrganizationDomain.DELETE;
                break;

            case GET_BY_PARENT_METHOD:
                organizationId = ((String)arguments[0]);
                action = OrganizationDomain.MANAGE_SUBORGANIZATIONS;
                break;

            case GET_ORGANIZATIONS_METHOD:
                final String userId = (String)arguments[0];
                if (userId != null
                    && !userId.equals(currentSubject.getUserId())
                    && !currentSubject.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_ORGANIZATIONS_ACTION)) {
                    throw new ForbiddenException("The user is able to specify only his own id");
                }
                //user specified his user id or has required permission
                return;

            //methods accessible to every user
            case GET_BY_ID_METHOD:
            case FIND_METHOD:
                return;

            default:
                throw new ForbiddenException("The user does not have permission to perform this operation");
        }

        if (currentSubject.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_ORGANIZATIONS_ACTION)) {
            //user is admin and he should be able to do anything with any organizations
            return;
        }

        //user is not admin and it is need to check permissions on organization instance level
        final Organization organization = manager.getById(organizationId);
        final String parentOrganizationId = organization.getParent();
        //check permissions on parent organization level when updating or removing child organization
        if (parentOrganizationId != null && (OrganizationDomain.UPDATE.equals(action) || OrganizationDomain.DELETE.equals(action))) {
            if (currentSubject.hasPermission(OrganizationDomain.DOMAIN_ID, parentOrganizationId, MANAGE_SUBORGANIZATIONS)) {
                //user has permissions to manage organization on parent organization level
                return;
            }
        }

        if (!currentSubject.hasPermission(DOMAIN_ID, organizationId, action)) {
            throw new ForbiddenException("The user does not have permission to " + action + " organization with id '"
                                         + organizationId + "'");
        }
    }
}
