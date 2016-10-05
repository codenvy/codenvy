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
package com.codenvy.resource.api;

import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.inject.Inject;
import javax.ws.rs.Path;

import static com.codenvy.organization.api.permissions.OrganizationDomain.CREATE_WORKSPACES;
import static com.codenvy.organization.api.permissions.OrganizationDomain.DOMAIN_ID;
import static com.codenvy.organization.api.permissions.OrganizationDomain.MANAGE_RESOURCES;
import static com.codenvy.organization.api.permissions.OrganizationDomain.MANAGE_WORKSPACES;
import static com.codenvy.organization.spi.impl.OrganizationImpl.ORGANIZATIONAL_ACCOUNT;
import static org.eclipse.che.api.user.server.model.impl.UserImpl.PERSONAL_ACCOUNT;

/**
 * Restricts access to methods of {@link ResourceService} by users' permissions.
 *
 * <p>Filter contains rules for protecting of all methods of {@link ResourceService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/resource{path:(?!/free)(/.*)?}")
public class ResourceServicePermissionsFilter extends CheMethodInvokerFilter {
    static final String GET_TOTAL_RESOURCES_METHOD     = "getTotalResources";
    static final String GET_AVAILABLE_RESOURCES_METHOD = "getAvailableResources";
    static final String GET_USED_RESOURCES_METHOD      = "getUsedResources";

    @Inject
    private AccountManager accountManager;

    @Override
    protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments) throws ApiException {
        String accountId;
        switch (genericMethodResource.getMethod().getName()) {
            case GET_TOTAL_RESOURCES_METHOD:
            case GET_AVAILABLE_RESOURCES_METHOD:
            case GET_USED_RESOURCES_METHOD:
                accountId = ((String)arguments[0]);
                break;

            default:
                throw new ForbiddenException("The user does not have permission to perform this operation");
        }

        if (!canSeeResources(EnvironmentContext.getCurrent().getSubject(), accountId)) {
            throw new ForbiddenException("User is not authorized to perform given operation");
        }
    }

    @VisibleForTesting
    boolean canSeeResources(Subject subject, String accountId) throws NotFoundException, ServerException {
        final Account account = accountManager.getById(accountId);
        if (ORGANIZATIONAL_ACCOUNT.equals(account.getType())) {
            if (subject.hasPermission(DOMAIN_ID, accountId, CREATE_WORKSPACES)
                || subject.hasPermission(DOMAIN_ID, accountId, MANAGE_RESOURCES)
                || subject.hasPermission(DOMAIN_ID, accountId, MANAGE_WORKSPACES)) {
                //user should be able to see resources of given account
                return true;
            }
        } else if (PERSONAL_ACCOUNT.equals(account.getType())) {
            if (subject.getUserId().equals(accountId)) {
                //user should be able to see resources of his personal account
                return true;
            }
        }

        return false;
    }
}
