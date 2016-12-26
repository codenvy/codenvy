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
package com.codenvy.api.workspace.server.filters;

import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.google.api.client.repackaged.com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.inject.Inject;
import javax.ws.rs.Path;

import static com.codenvy.api.workspace.server.WorkspaceDomain.CONFIGURE;
import static com.codenvy.api.workspace.server.WorkspaceDomain.DELETE;
import static com.codenvy.api.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static com.codenvy.api.workspace.server.WorkspaceDomain.READ;
import static com.codenvy.api.workspace.server.WorkspaceDomain.RUN;
import static com.codenvy.api.workspace.server.WorkspaceDomain.USE;
import static com.codenvy.organization.api.permissions.OrganizationDomain.CREATE_WORKSPACES;
import static com.codenvy.organization.api.permissions.OrganizationDomain.MANAGE_WORKSPACES;
import static com.codenvy.organization.spi.impl.OrganizationImpl.ORGANIZATIONAL_ACCOUNT;

/**
 * Restricts access to methods of {@link WorkspaceService} by users' permissions
 *
 * <p>Filter contains rules for protecting of all methods of {@link WorkspaceService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/workspace{path:(/.*)?}")
public class WorkspacePermissionsFilter extends CheMethodInvokerFilter {
    private final WorkspaceManager workspaceManager;
    private final AccountManager   accountManager;

    @Inject
    public WorkspacePermissionsFilter(WorkspaceManager workspaceManager, AccountManager accountManager) {
        this.workspaceManager = workspaceManager;
        this.accountManager = accountManager;
    }

    @Override
    public void filter(GenericResourceMethod genericResourceMethod, Object[] arguments) throws ForbiddenException,
                                                                                               ServerException,
                                                                                               NotFoundException {
        final String methodName = genericResourceMethod.getMethod().getName();

        final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
        String action;
        String key;

        switch (methodName) {
            case "getSettings":
            case "getWorkspaces":
                //methods accessible to every user
                return;

            case "getByNamespace": {
                if (currentSubject.hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION)) {
                    return;
                }
                checkManageNamespaceAccess(currentSubject, ((String)arguments[1]));
                return;
            }

            case "create": {
                checkNamespaceAccess(currentSubject, ((String)arguments[3]), MANAGE_WORKSPACES, CREATE_WORKSPACES);
                return;
            }

            case "startFromConfig": {
                checkManageNamespaceAccess(currentSubject, ((String)arguments[2]));
                return;
            }

            case "delete":
                key = ((String)arguments[0]);
                action = DELETE;
                break;

            case "stop":
                if (currentSubject.hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION)) {
                    return;
                }
            case "startById":
            case "createSnapshot":
                key = ((String)arguments[0]);
                action = RUN;
                break;

            case "getSnapshot":
                key = ((String)arguments[0]);
                action = READ;
                break;

            case "getByKey":
                if (currentSubject.hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION)) {
                    return;
                }
            case "checkAgentHealth":
                key = ((String)arguments[0]);
                action = READ;
                break;

            case "update":
            case "addProject":
            case "deleteProject":
            case "updateProject":
            case "addEnvironment":
            case "deleteEnvironment":
            case "updateEnvironment":
            case "addCommand":
            case "deleteCommand":
            case "updateCommand":
                key = ((String)arguments[0]);
                action = CONFIGURE;
                break;

            // MachineService methods
            case "startMachine":
            case "stopMachine":
                key = ((String)arguments[0]);
                action = RUN;
                break;

            case "getMachineById":
            case "getMachines":
            case "executeCommandInMachine":
            case "getProcesses":
            case "stopProcess":
            case "getProcessLogs":
                key = ((String)arguments[0]);
                action = USE;
                break;

            default:
                throw new ForbiddenException("The user does not have permission to perform this operation");
        }

        final WorkspaceImpl workspace = workspaceManager.getWorkspace(key);
        try {
            checkManageNamespaceAccess(currentSubject, workspace.getNamespace());
            // user is authorized to perform any operation if workspace belongs to account where he has the corresponding permissions
        } catch (ForbiddenException e) {
            //check permissions on workspace level
            if (!currentSubject.hasPermission(DOMAIN_ID, workspace.getId(), action)) {
                throw new ForbiddenException(
                    "The user does not have permission to " + action + " workspace with id '" + workspace.getId() + "'");
            }
        }
    }

    private void checkManageNamespaceAccess(Subject currentSubject, @Nullable String namespace) throws ServerException,
                                                                                                       NotFoundException,
                                                                                                       ForbiddenException {
        checkNamespaceAccess(currentSubject, namespace, MANAGE_WORKSPACES);
    }

    @VisibleForTesting
    void checkNamespaceAccess(Subject currentSubject, @Nullable String namespace, String... actions) throws ForbiddenException,
                                                                                                            NotFoundException,
                                                                                                            ServerException {
        if (namespace == null) {
            //namespace will be defined as username by default
            return;
        }

        final Account account = accountManager.getByName(namespace);

        if (UserImpl.PERSONAL_ACCOUNT.equals(account.getType())) {
            if (!account.getName().equals(currentSubject.getUserName())) {
                throw new ForbiddenException("User is not authorized to use given namespace");
            }
        } else if (ORGANIZATIONAL_ACCOUNT.equals(account.getType())) {
            boolean authorized = false;
            for (String action : actions) {
                if (authorized = currentSubject.hasPermission(OrganizationDomain.DOMAIN_ID, account.getId(), action)) {
                    break;
                }
            }
            if (!authorized) {
                throw new ForbiddenException("User is not authorized to use given namespace");
            }
        } else {
            throw new ForbiddenException("User is not authorized to use given namespace");
        }
    }
}
