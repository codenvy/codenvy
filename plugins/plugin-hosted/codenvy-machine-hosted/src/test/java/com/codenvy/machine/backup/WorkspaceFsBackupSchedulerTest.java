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
package com.codenvy.machine.backup;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WorkspaceFsBackupSchedulerTest {
    private static final int    FAKE_BACKUP_TIME_MS = 1500;
    private static final String WORKSPACE_ID_1      = "testWorkspaceId-1";
    private static final String WORKSPACE_ID_2      = "testWorkspaceId-2";
    private static final String ACTIVE_ENV          = "testActiveEnv";
    private static final String ENV_TYPE            = "testEnvType";
    private static final long   BACKUP_TIMEOUT      = 1;// 1 second

    @Mock
    private WorkspaceRuntimes        workspaceRuntimes;
    @Mock
    private WorkspaceManager         workspaceManager;
    @Mock
    private EnvironmentBackupManager backupManager;

    private WorkspaceConfigImpl                           workspaceConfig;
    private WorkspaceFsBackupScheduler                    scheduler;
    private Map<String, WorkspaceRuntimes.WorkspaceState> workspaces;

    @BeforeMethod
    public void setUp() throws Exception {
        scheduler = spy(new WorkspaceFsBackupScheduler(Collections.singletonMap(ENV_TYPE, backupManager),
                                                       workspaceRuntimes,
                                                       BACKUP_TIMEOUT,
                                                       workspaceManager));

        EnvironmentImpl environment = new EnvironmentImpl();
        EnvironmentRecipeImpl environmentRecipe = new EnvironmentRecipeImpl();
        environmentRecipe.setType(ENV_TYPE);
        environment.setRecipe(environmentRecipe);
        WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
        workspaceConfig.setEnvironments(Collections.singletonMap(ACTIVE_ENV, environment));
        this.workspaceConfig = spy(workspaceConfig);

        workspaces = new LinkedHashMap<>();
        addWorkspace(WORKSPACE_ID_1);
        addWorkspace(WORKSPACE_ID_2);
        when(workspaceRuntimes.getWorkspaces()).thenReturn(workspaces);
    }

    private WorkspaceImpl addWorkspace(String wsId) throws Exception {
        WorkspaceRuntimes.WorkspaceState workspaceState =
                new WorkspaceRuntimes.WorkspaceState(WorkspaceStatus.RUNNING, ACTIVE_ENV);
        WorkspaceImpl workspace = mock(WorkspaceImpl.class);
        WorkspaceRuntimeImpl workspaceRuntime = mock(WorkspaceRuntimeImpl.class);

        when(workspaceManager.getWorkspace(wsId)).thenReturn(workspace);
        when(workspace.getRuntime()).thenReturn(workspaceRuntime);
        when(workspace.getStatus()).thenReturn(WorkspaceStatus.RUNNING);
        when(workspace.getConfig()).thenReturn(workspaceConfig);
        when(workspaceRuntime.getActiveEnv()).thenReturn(ACTIVE_ENV);

        workspaces.put(wsId, workspaceState);

        return workspace;
    }

    @Test
    public void shouldBackupAllWorkspaces() throws Exception {
        // when
        scheduler.scheduleBackup();

        // then
        for (Map.Entry<String, WorkspaceRuntimes.WorkspaceState> workspaceState : workspaces.entrySet()) {
            verify(backupManager, timeout(2000)).backupWorkspace(workspaceState.getKey());
        }
    }

    @Test
    public void shouldBackupOtherWorkspacesIfBackupOfPreviousFails() throws Exception {
        // given
        addWorkspace("testWsId3");
        addWorkspace("testWsId4");
        addWorkspace("testWsId5");
        doThrow(new ServerException("server exception")).when(backupManager).backupWorkspace(eq("testWsId3"));

        // when
        scheduler.scheduleBackup();

        // then
        verify(backupManager, timeout(1000)).backupWorkspace(WORKSPACE_ID_1);
        verify(backupManager, timeout(1000)).backupWorkspace(WORKSPACE_ID_2);
        verify(backupManager, timeout(1000)).backupWorkspace("testWsId3");
        verify(backupManager, timeout(1000)).backupWorkspace("testWsId4");
        verify(backupManager, timeout(1000)).backupWorkspace("testWsId5");
    }

    @Test
    public void shouldNotBackupWsIfBackupManagerNotFound() throws Exception {
        // given
        workspaces.clear();
        String wsId = "testWsId";
        WorkspaceImpl workspace = addWorkspace(wsId);
        workspace.getConfig().getEnvironments().values().iterator().next().getRecipe().setType("somethingElse");

        // when
        scheduler.scheduleBackup();

        // then
        verify(backupManager, timeout(2000).never()).backupWorkspace(wsId);
    }

    @Test
    public void shouldNotBackupWSWithNonRunningStatus() throws Exception {
        // given
        WorkspaceImpl wsInSnapshottingState = addWorkspace("ws3");
        when(workspaceManager.getWorkspace("ws3")).thenReturn(wsInSnapshottingState);
        when(wsInSnapshottingState.getStatus()).thenReturn(WorkspaceStatus.SNAPSHOTTING);
        WorkspaceImpl wsInStoppingState = addWorkspace("ws4");
        when(workspaceManager.getWorkspace("ws4")).thenReturn(wsInStoppingState);
        when(wsInStoppingState.getStatus()).thenReturn(WorkspaceStatus.STOPPING);
        WorkspaceImpl wsInStartingState = addWorkspace("ws5");
        when(workspaceManager.getWorkspace("ws5")).thenReturn(wsInStartingState);
        when(wsInStartingState.getStatus()).thenReturn(WorkspaceStatus.STARTING);

        // when
        scheduler.scheduleBackup();

        // then
        // add this verification with timeout to ensure that thread executor had enough time before verification of call
        verify(backupManager, timeout(2000)).backupWorkspace(WORKSPACE_ID_1);
        verify(backupManager, timeout(2000)).backupWorkspace(WORKSPACE_ID_2);
        verify(backupManager, timeout(2000).never()).backupWorkspace("ws3");
        // previous verifying should wait enough, so no timeouts here
        verify(backupManager, never()).backupWorkspace("ws4");
        verify(backupManager, never()).backupWorkspace("ws5");
    }

    @Test
    public void shouldNotBackupMachineIfElapsedTimeFromLastSyncTooSmall() throws Exception {
        // given
        workspaces.clear();
        addWorkspace("ws3");
        scheduler.scheduleBackup();

        // add this verification with timeout to ensure that thread executor had enough time before verification of call
        verify(backupManager, timeout(2000)).backupWorkspace("ws3");

        // when
        // second synchronization
        scheduler.scheduleBackup();

        verify(workspaceRuntimes, times(2)).getWorkspaces();
        verify(scheduler, times(2)).isTimeToBackup("ws3");
        Thread.sleep(1000);
        verify(backupManager, timeout(2000)).backupWorkspace(eq("ws3"));
    }

    @Test
    public void shouldBackupMachineFsIfLastSyncTimeoutIsExpired() throws Exception {
        // given
        workspaces.clear();
        addWorkspace("ws3");
        scheduler = spy(new WorkspaceFsBackupScheduler(Collections.singletonMap(ENV_TYPE, backupManager),
                                                       workspaceRuntimes,
                                                       0,
                                                       workspaceManager));

        scheduler.scheduleBackup();

        // wait until previous backup finish
        verify(backupManager, timeout(1000)).backupWorkspace(eq("ws3"));
        Thread.sleep(100);

        // when
        // second synchronization
        scheduler.scheduleBackup();

        // then
        verify(workspaceRuntimes, times(2)).getWorkspaces();
        verify(backupManager, timeout(2000).times(2)).backupWorkspace(eq("ws3"));
    }

    @Test
    public void shouldNotBackupMachineFsIfPreviousBackupIsStillRunning() throws Exception {
        // given
        workspaces.clear();
        addWorkspace("ws3");
        scheduler = spy(new WorkspaceFsBackupScheduler(Collections.singletonMap(ENV_TYPE, backupManager),
                                                       workspaceRuntimes,
                                                       0,
                                                       workspaceManager));
        doAnswer(invocation -> {
            sleep(FAKE_BACKUP_TIME_MS);
            return null;
        }).when(backupManager).backupWorkspace("ws3");

        // when
        scheduler.scheduleBackup();
        sleep(FAKE_BACKUP_TIME_MS / 2);
        // run next backup while previous is still running
        scheduler.scheduleBackup();

        // then
        verify(workspaceRuntimes, times(2)).getWorkspaces();
        verify(backupManager, timeout(2000)).backupWorkspace(eq("ws3"));
    }
}
