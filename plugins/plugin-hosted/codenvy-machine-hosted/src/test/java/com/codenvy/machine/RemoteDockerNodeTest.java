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
package com.codenvy.machine;

import com.codenvy.machine.backup.DockerEnvironmentBackupManager;

import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Igor Vinokur
 */
@Listeners(MockitoTestNGListener.class)
public class RemoteDockerNodeTest {
    private static final String PATH = "WorkspacePath";

    @Mock
    private DockerEnvironmentBackupManager backupManager;
    @Mock
    private DockerConnector                dockerConnector;
    @Mock
    private WorkspaceFolderPathProvider    pathProvider;
    @Mock
    private Exec                           exec;

    private RemoteDockerNode remoteDockerNode;

    @BeforeMethod
    public void setUp() throws Exception {
        when(pathProvider.getPath("WorkspaceId")).thenReturn(PATH);
        when(dockerConnector.createExec(any())).thenReturn(exec);
        when(exec.getId()).thenReturn("ExecId");
        remoteDockerNode = new RemoteDockerNode(dockerConnector,
                                                "ContainerId",
                                                "WorkspaceId",
                                                backupManager);
    }

    @Test
    public void shouldRestoreWorkspaceBackupOnNodeBinding() throws Exception {
        //when
        remoteDockerNode.bindWorkspace();

        //then
        verify(backupManager).restoreWorkspaceBackup(eq("WorkspaceId"),
                                                     eq("ContainerId"),
                                                     eq("127.0.0.1"));
    }

    @Test
    public void shouldBackupAndCleanupWSOnNodeUnbinding() throws Exception {
        //when
        remoteDockerNode.unbindWorkspace();

        //then
        verify(backupManager).backupWorkspaceAndCleanup(eq("WorkspaceId"),
                                                        eq("ContainerId"),
                                                        eq("127.0.0.1"));
    }
}
