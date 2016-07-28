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

import com.google.api.client.repackaged.com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
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

    @Inject
    public WorkspacePermissionsFilter(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
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
            case "getWorkspaces": //method accessible to every user
                return;

            case "getByNamespace": {
                checkNamespaceAccess(currentSubject, ((String)arguments[1]));
                return;
            }

            case "create": {
                checkNamespaceAccess(currentSubject, ((String)arguments[3]));
                return;
            }

            case "startFromConfig": {
                checkNamespaceAccess(currentSubject, ((String)arguments[2]));
                return;
            }

            case "delete":
                key = ((String)arguments[0]);
                action = DELETE;
                break;
            case "createMachine":
            case "stop":
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

            default:
                throw new ForbiddenException("The user does not have permission to perform this operation");
        }

        final WorkspaceImpl workspace = workspaceManager.getWorkspace(key);
        final String namespace = workspace.getNamespace();

        if (currentSubject.getUserName().equals(namespace)) {
            // user is authorized to perform any operation if workspace belongs to his personal account
            return;
        }

        if (!currentSubject.hasPermission(DOMAIN_ID, workspace.getId(), action)) {
            throw new ForbiddenException("The user does not have permission to " + action + " workspace with id '" + workspace.getId() + "'");
        }
    }

    @VisibleForTesting
    void checkNamespaceAccess(Subject currentSubject, String namespace) throws ForbiddenException {
        if (namespace == null) {
            //namespace will be defined as username by default
            return;
        }

        if (!currentSubject.getUserName().equals(namespace)) {
            throw new ForbiddenException("User is not authorized to use given namespace");
        }
    }
}
