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
package com.codenvy.api.audit.server.printer;

import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ComparisonChain.start;
import static java.util.Collections.sort;

/**
 * Prints users info and his related workspaces info in format:
 * <p>
 * <user e-mail> is owner of <own workspaces number> workspace(s) and has permissions in <workspaces number> workspace(s)
 * └ <workspace name>, is owner: <true or false>, permissions: <list of permissions for current user>
 * .
 * .
 * .
 * └ <workspace name>, is owner: <true or false>, permissions: <list of permissions for current user>
 * <p>
 * If the list of user workspaces is not defined, it prints error message.
 *
 * @author Dmytro Nochevnov
 * @author Igor Vinokur
 */
public class UserInfoPrinter extends Printer {

    private UserImpl user;
    private List<WorkspaceImpl> workspaces;
    private Map<String, AbstractPermissions> wsPermissions;

    /**
     * @param auditReport
     *         file of audit report
     * @param user
     *         information about user collected in {@link UserImpl} object
     * @param workspaces
     *         list of workspaces that are related to given user
     * @param wsPermissions
     *         map of permissions to workspaces
     */
    public UserInfoPrinter(Path auditReport,
                           UserImpl user,
                           List<WorkspaceImpl> workspaces,
                           Map<String, AbstractPermissions> wsPermissions) {
        super(auditReport);

        this.user = user;
        this.workspaces = workspaces;
        this.wsPermissions = wsPermissions;
    }

    @Override
    public void print() throws ServerException {
        long permissionsNumber = wsPermissions.values().stream().filter(permissions -> permissions != null).count();
        long ownWorkspacesNumber = workspaces.stream().filter(workspace -> workspace.getNamespace().equals(user.getName())).count();
        printRow(user.getEmail() + " is owner of " +
                 ownWorkspacesNumber + " workspace" + (ownWorkspacesNumber > 1 | ownWorkspacesNumber == 0 ? "s" : "") +
                 " and has permissions in " + permissionsNumber + " workspace" +
                 (permissionsNumber > 1 | permissionsNumber == 0 ? "s" : "") + "\n");
        sort(workspaces, (ws1, ws2) -> start().compareTrueFirst(ws1.getNamespace().equals(user.getName()),
                                                                ws2.getNamespace().equals(user.getName()))
                                              .compare(ws1.getConfig().getName(), ws2.getConfig().getName())
                                              .result());
        for (WorkspaceImpl workspace : workspaces) {
            printUserWorkspaceInfo(workspace);
        }
    }

    private void printUserWorkspaceInfo(WorkspaceImpl workspace) throws ServerException {
        printRow("   └ " + workspace.getConfig().getName() +
                 ", is owner: " + workspace.getNamespace().equals(user.getName()) + ", permissions: ");
        AbstractPermissions permissions = wsPermissions.get(workspace.getId());
        if (permissions != null) {
            printRow(permissions.getActions().toString() + "\n");
        } else {
            printRow("[]\n");
        }
    }
}
