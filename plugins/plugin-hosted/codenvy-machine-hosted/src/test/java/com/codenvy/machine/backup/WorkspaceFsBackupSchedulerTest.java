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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceNode;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
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

    @Mock
    private WorkspaceRuntimes workspaceRuntimes;

    @Mock
    private MachineBackupManager backupManager;

    private WorkspaceFsBackupScheduler scheduler;

    MachineImpl machine1;

    MachineImpl machine2;

    @Mock
    private Instance machineInstance;

    @Mock
    private InstanceNode node;

    private Map<String, WorkspaceRuntimes.WorkspaceState> workspaces;

    @BeforeMethod
    public void setUp() throws Exception {
        scheduler = spy(new WorkspaceFsBackupScheduler(workspaceRuntimes, backupManager, 5 * 60));

        doNothing().when(scheduler).backupWorkspaceInMachine(any(MachineImpl.class));

        workspaces = new LinkedHashMap<>();

        machine1 = addWorkspace(WORKSPACE_ID_1, "id1");
        machine2 = addWorkspace(WORKSPACE_ID_2, "id2");
        when(workspaceRuntimes.getWorkspaces()).thenReturn(workspaces);


        when(machineInstance.getStatus()).thenReturn(MachineStatus.RUNNING);
    }

    private MachineImpl addWorkspace(String wsId, String machineId) throws Exception {
        MachineImpl devMachine = mock(MachineImpl.class);
        when(devMachine.getId()).thenReturn(machineId);
        when(devMachine.getWorkspaceId()).thenReturn(wsId);
        WorkspaceRuntimes.RuntimeDescriptor runtimeDescriptor = mock(WorkspaceRuntimes.RuntimeDescriptor.class);
        WorkspaceRuntimes.WorkspaceState workspaceState =
                new WorkspaceRuntimes.WorkspaceState(WorkspaceStatus.RUNNING, "some_env");
        workspaces.put(wsId, workspaceState);
        when(runtimeDescriptor.getRuntimeStatus()).thenReturn(WorkspaceStatus.RUNNING);
        WorkspaceRuntimeImpl workspaceRuntime = mock(WorkspaceRuntimeImpl.class);
        when(runtimeDescriptor.getRuntime()).thenReturn(workspaceRuntime);
        when(workspaceRuntime.getDevMachine()).thenReturn(devMachine);
        when(workspaceRuntimes.get(wsId)).thenReturn(runtimeDescriptor);
        when(workspaceRuntimes.getMachine(wsId, machineId)).thenReturn(machineInstance);

        return devMachine;
    }

    @Test
    public void shouldBackupWs() throws Exception {
        // given
        when(workspaceRuntimes.getMachine(WORKSPACE_ID_1, "id1")).thenReturn(machineInstance);
        when(machineInstance.getNode()).thenReturn(node);
        when(node.getHost()).thenReturn("192.168.0.1");
        when(node.getProjectsFolder()).thenReturn("/" + WORKSPACE_ID_1);
        doCallRealMethod().when(scheduler).backupWorkspaceInMachine(any(MachineImpl.class));
        MachineImpl devMachine = mock(MachineImpl.class);
        when(devMachine.getId()).thenReturn("id1");
        when(devMachine.getWorkspaceId()).thenReturn(WORKSPACE_ID_1);

        // when
        scheduler.backupWorkspaceInMachine(devMachine);

        // then
        verify(workspaceRuntimes).getMachine(WORKSPACE_ID_1, "id1");
        verify(scheduler).backupWorkspaceInMachine(any(MachineImpl.class));
        verify(backupManager).backupWorkspace(WORKSPACE_ID_1, "/" + WORKSPACE_ID_1, "192.168.0.1", "22");
    }

    @Test
    public void shouldBackupWorkspaceFsOfMachines() throws Exception {
        // when
        scheduler.scheduleBackup();

        // then
        for (Map.Entry<String, WorkspaceRuntimes.WorkspaceState> workspaceState : workspaces.entrySet()) {
            verify(scheduler, timeout(2000))
                    .backupWorkspaceInMachine(eq(workspaceRuntimes.get(workspaceState.getKey())
                                                                  .getRuntime()
                                                                  .getDevMachine()));
        }
    }

    @Test
    public void shouldBackupOtherWorkspacesIfBackupOfPreviousFails() throws Exception {
        // given
        MachineImpl machine3 = addWorkspace("testWsId3", "id3");
        MachineImpl machine4 = addWorkspace("testWsId4", "id4");
        MachineImpl machine5 = addWorkspace("testWsId5", "id5");
        doThrow(new ServerException("server exception")).when(scheduler).backupWorkspaceInMachine(eq(machine1));

        // when
        scheduler.scheduleBackup();

        // then
        verify(scheduler, timeout(1000)).backupWorkspaceInMachine(eq(machine1));
        verify(scheduler, timeout(1000)).backupWorkspaceInMachine(eq(machine2));
        verify(scheduler, timeout(1000)).backupWorkspaceInMachine(eq(machine3));
        verify(scheduler, timeout(1000)).backupWorkspaceInMachine(eq(machine4));
        verify(scheduler, timeout(1000)).backupWorkspaceInMachine(eq(machine5));
    }

    @Test
    public void shouldNotBackupMachinesWithNonRunningStatus() throws Exception {
        // given
        MachineImpl creating = addWorkspace("ws3", "ms3");
        Instance creatingInstance = mock(Instance.class);
        when(creating.getStatus()).thenReturn(MachineStatus.CREATING);
        when(creatingInstance.getNode()).thenReturn(node);
        when(workspaceRuntimes.getMachine("ws3", "ms3")).thenReturn(creatingInstance);

        MachineImpl destroying = addWorkspace("ws4", "ms4");
        Instance destroyingInstance = mock(Instance.class);
        when(destroying.getStatus()).thenReturn(MachineStatus.DESTROYING);
        when(destroyingInstance.getNode()).thenReturn(node);
        when(workspaceRuntimes.getMachine("ws4", "ms4")).thenReturn(destroyingInstance);

        when(workspaceRuntimes.getMachine(WORKSPACE_ID_1, "id1")).thenReturn(machineInstance);
        when(machineInstance.getNode()).thenReturn(node);
        when(node.getHost()).thenReturn("192.168.0.1");
        when(node.getProjectsFolder()).thenReturn("/" + WORKSPACE_ID_1);
        doNothing().when(backupManager).backupWorkspace(anyString(), anyString(), anyString(), anyString());
        doCallRealMethod().when(scheduler).backupWorkspaceInMachine(any(MachineImpl.class));

        // when
        scheduler.scheduleBackup();

        // then
        // add this verification with timeout to ensure that thread executor had enough time before verification of call
        verify(backupManager, timeout(1000)).backupWorkspace(eq(WORKSPACE_ID_1), anyString(), anyString(), anyString());
        verify(backupManager, timeout(1000).never()).backupWorkspace(eq("ws3"), anyString(), anyString(), anyString());
        verify(backupManager, timeout(1000).never()).backupWorkspace(eq("ws4"), anyString(), anyString(), anyString());
    }

    @Test
    public void shouldNotBackupAnythingIfWorkspaceRetrievalFails() throws Exception {
        // given
        when(workspaceRuntimes.get(anyString())).thenThrow(new NotFoundException(""));

        // when
        scheduler.scheduleBackup();

        // then
        verify(workspaceRuntimes).getWorkspaces();
        verify(workspaceRuntimes, times(2)).get(anyString());
        verify(scheduler, never()).backupWorkspaceInMachine(any(MachineImpl.class));
    }

    @Test
    public void shouldNotBackupMachineIfElapsedTimeFromLastSyncTooSmall() throws Exception {
        // given
        workspaces.clear();
        MachineImpl machine = addWorkspace("ws3", "ms3");
        scheduler.scheduleBackup();

        // add this verification with timeout to ensure that thread executor had enough time before verification of call
        verify(scheduler, timeout(2000)).backupWorkspaceInMachine(eq(machine));

        // when
        // second synchronization
        scheduler.scheduleBackup();

        verify(workspaceRuntimes, times(2)).get(anyString());
        verify(scheduler, times(2)).isTimeToBackup(machine.getId());
        verify(scheduler, timeout(2000)).backupWorkspaceInMachine(eq(machine));
    }

    @Test
    public void shouldBackupMachineFsIfLastSyncTimeoutIsExpired() throws Exception {
        // given
        workspaces.clear();
        MachineImpl machine = addWorkspace("ws3", "ms3");
        scheduler = spy(new WorkspaceFsBackupScheduler(workspaceRuntimes, backupManager, 0));
        doNothing().when(scheduler).backupWorkspaceInMachine(any(MachineImpl.class));

        scheduler.scheduleBackup();

        // wait until previous backup finish
        sleep(100);

        // when
        // second synchronization
        scheduler.scheduleBackup();

        // then
        verify(workspaceRuntimes, times(2)).get(anyString());
        verify(scheduler, timeout(2000).times(2)).backupWorkspaceInMachine(eq(machine));
    }

    @Test
    public void shouldNotBackupMachineFsIfPreviousBackupIsStillRunning() throws Exception {
        // given
        workspaces.clear();
        MachineImpl machine = addWorkspace("ws3", "ms3");
        scheduler = spy(new WorkspaceFsBackupScheduler(workspaceRuntimes, backupManager, 0));
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                sleep(FAKE_BACKUP_TIME_MS);
                return null;
            }
        }).when(scheduler).backupWorkspaceInMachine(any(MachineImpl.class));

        // when
        scheduler.scheduleBackup();
        sleep(FAKE_BACKUP_TIME_MS / 2);
        // run next backup while previous is still running
        scheduler.scheduleBackup();

        // then
        verify(workspaceRuntimes, times(2)).get(anyString());
        verify(scheduler, timeout(2000).times(1)).backupWorkspaceInMachine(eq(machine));
    }

    @Test
    public void shouldSkipWorkspaceBackupIfMachineAlreadyStopped() throws Exception {
        // given
        when(workspaceRuntimes.getMachine(anyString(), anyString())).thenThrow(new NotFoundException(""));

        // when
        scheduler.scheduleBackup();

        // then
        verify(backupManager, never()).backupWorkspace(anyString(), anyString(), anyString(), anyString());
    }

}
