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

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.MachineService;
import org.eclipse.che.api.machine.server.recipe.RecipeService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericMethodResource;

import javax.inject.Inject;
import javax.ws.rs.Path;

import static com.codenvy.api.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static com.codenvy.api.workspace.server.WorkspaceDomain.RUN;
import static com.codenvy.api.workspace.server.WorkspaceDomain.USE;

/**
 * Restricts access to methods of {@link MachineService} by users' permissions
 *
 * <p>Filter contains rules for protecting of all methods of {@link MachineService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/machine{path:(?!/token)(/.*)?}")
public class MachinePermissionsFilter extends CheMethodInvokerFilter {
    @Inject
    private MachineManager machineManager;

    @Override
    protected void filter(GenericMethodResource genericMethodResource, Object[] arguments) throws ApiException {
        final String methodName = genericMethodResource.getMethod().getName();

        final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
        String action;
        String workspaceId;

        switch (methodName) {
            case "copyFilesBetweenMachines": {
                String sourceMachineId = ((String)arguments[0]);
                String targetMachineId = ((String)arguments[1]);
                String sourceWorkspaceId = machineManager.getMachine(sourceMachineId).getWorkspaceId();
                String targetWorkspaceId = machineManager.getMachine(targetMachineId).getWorkspaceId();
                currentSubject.checkPermission(DOMAIN_ID, sourceWorkspaceId, USE);
                currentSubject.checkPermission(DOMAIN_ID, targetWorkspaceId, USE);
                return;
            }

            case "getMachines":
            case "getSnapshots": {
                workspaceId = ((String)arguments[0]);
                action = USE;
                break;
            }

            case "executeCommandInMachine":
            case "getProcesses":
            case "stopProcess":
            case "getMachineLogs":
            case "getProcessLogs":
            case "getFileContent":
            case "getMachineById": {
                String machineId = ((String)arguments[0]);
                workspaceId = machineManager.getMachine(machineId).getWorkspaceId();
                action = USE;
                break;
            }

            case "saveSnapshot":
            case "destroyMachine": {
                String machineId = ((String)arguments[0]);
                workspaceId = machineManager.getMachine(machineId).getWorkspaceId();
                action = RUN;
                break;
            }

            case "removeSnapshot": {
                String snapshotId = ((String)arguments[0]);
                workspaceId = machineManager.getSnapshot(snapshotId).getWorkspaceId();
                action = RUN;
                break;
            }

            default:
                throw new ForbiddenException("The user does not have permission to perform this operation");
        }

        currentSubject.checkPermission(DOMAIN_ID, workspaceId, action);
    }
}
