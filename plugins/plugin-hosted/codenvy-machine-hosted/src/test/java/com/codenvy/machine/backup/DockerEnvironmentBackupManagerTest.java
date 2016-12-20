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
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.MessageProcessor;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.plugin.docker.machine.DockerInstance;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

/**
 * @author Mykola Morhun
 */
@Listeners(value = {MockitoTestNGListener.class})
public class DockerEnvironmentBackupManagerTest {
    private static final Logger LOG = getLogger(DockerEnvironmentBackupManagerTest.class);

    private static final String WORKSPACE_ID                   = "workspaceId";
    private static final String CONTAINER_ID                   = "containerId";
    private static final String BACKUP_SCRIPT                  = "/tmp/backup.sh";
    private static final String RESTORE_SCRIPT                 = "/tmp/restore.sh";
    private static final int    MAX_BACKUP_DURATION_SEC        = 10;
    private static final int    MAX_RESTORE_DURATION_SEC       = 10;
    private static final String BACKUPS_ROOT_PATH              = "/tmp/che/backups";
    private static final String SRC_PATH                       = "/some/path/on/docker/node";
    private static final String USER_ID                        = "1234";
    private static final String USER_GID                       = "12345";
    private static final String PATH_TO_WORKSPACE              = "00/00/00/";
    private static final String ABSOLUTE_PATH_TO_WORKSPACE_DIR = BACKUPS_ROOT_PATH + PATH_TO_WORKSPACE + WORKSPACE_ID;
    private static final String NODE_HOST                      = "192.19.20.78";
    private static final String SYNC_STRATEGY                  = "rsync";
    private static final String PROJECTS_PATH_IN_CONTAINER     = "/projects-folder";
    private static final String USER_IN_CONTAINER              = "test-user";

    private static final String[] BACKUP_WORKSPACE_COMMAND              = {BACKUP_SCRIPT,
                                                                           SRC_PATH,
                                                                           NODE_HOST,
                                                                           "0",
                                                                           ABSOLUTE_PATH_TO_WORKSPACE_DIR,
                                                                           "false",
                                                                           USER_IN_CONTAINER};
    private static final String[] BACKUP_WORKSPACE_WITH_CLEANUP_COMMAND = {BACKUP_SCRIPT,
                                                                           SRC_PATH,
                                                                           NODE_HOST,
                                                                           "0",
                                                                           ABSOLUTE_PATH_TO_WORKSPACE_DIR,
                                                                           "true",
                                                                           USER_IN_CONTAINER};
    private static final String[] RESTORE_WORKSPACE_COMMAND             = {RESTORE_SCRIPT,
                                                                           ABSOLUTE_PATH_TO_WORKSPACE_DIR,
                                                                           SRC_PATH,
                                                                           NODE_HOST,
                                                                           "0",
                                                                           USER_ID,
                                                                           USER_GID,
                                                                           USER_IN_CONTAINER};

    @Mock
    private WorkspaceIdHashLocationFinder       workspaceIdHashLocationFinder;
    @Mock
    private WorkspaceFolderPathProvider         workspaceFolderPathProvider;
    @Mock
    private WorkspaceRuntimes                   workspaceRuntimes;
    @Mock
    private DockerConnector                     docker;
    @Mock
    private WorkspaceRuntimes.RuntimeDescriptor runtimeDescriptor;
    @Mock
    private WorkspaceRuntimeImpl                workspaceRuntime;
    @Mock
    private MachineImpl                         devMachine;
    @Mock
    private DockerInstance                      dockerInstance;
    @Mock
    private DockerNode                          dockerNode;
    @Mock
    private MachineRuntimeInfoImpl              machineRuntimeInfo;

    @Captor
    private ArgumentCaptor<String[]> cmdCaptor;

    private ExecutorService executor;

    private DockerEnvironmentBackupManager backupManager;

    @BeforeMethod
    private void setup() throws Exception {
        backupManager = spy(new DockerEnvironmentBackupManager(BACKUP_SCRIPT,
                                                               RESTORE_SCRIPT,
                                                               MAX_BACKUP_DURATION_SEC,
                                                               MAX_RESTORE_DURATION_SEC,
                                                               new File(BACKUPS_ROOT_PATH),
                                                               workspaceIdHashLocationFinder,
                                                               SYNC_STRATEGY,
                                                               PROJECTS_PATH_IN_CONTAINER,
                                                               workspaceRuntimes,
                                                               workspaceFolderPathProvider,
                                                               docker));

        when(workspaceRuntimes.get(anyString())).thenReturn(runtimeDescriptor);
        when(runtimeDescriptor.getRuntime()).thenReturn(workspaceRuntime);
        when(workspaceRuntime.getDevMachine()).thenReturn(devMachine);
        when(devMachine.getStatus()).thenReturn(MachineStatus.RUNNING);
        when(workspaceRuntimes.getMachine(anyString(), anyString())).thenReturn(dockerInstance);
        when(dockerInstance.getNode()).thenReturn(dockerNode);
        when(dockerNode.getHost()).thenReturn(NODE_HOST);
        when(dockerInstance.getContainer()).thenReturn(CONTAINER_ID);
        when(dockerInstance.getRuntime()).thenReturn(machineRuntimeInfo);
        when(workspaceFolderPathProvider.getPath(WORKSPACE_ID)).thenReturn(SRC_PATH);
        when(workspaceIdHashLocationFinder.calculateDirPath(any(File.class), any(String.class)))
                .thenReturn(new File(ABSOLUTE_PATH_TO_WORKSPACE_DIR));
        doNothing().when(backupManager).executeCommand(anyObject(), anyInt(), anyString());
        Exec getUserIdsExecMock = mock(Exec.class);
        when(getUserIdsExecMock.getId()).thenReturn("getUserIdsExecMockId");
        Exec getUserNameExecMock = mock(Exec.class);
        when(getUserNameExecMock.getId()).thenReturn("getUserNameExecMockId");
        when(docker.createExec(
                eq(CreateExecParams.create(CONTAINER_ID, new String[] {"sh", "-c", "id -u && id -g && id -u -n"})
                                   .withDetach(false)))).thenReturn(getUserIdsExecMock);
        when(docker.createExec(eq(CreateExecParams.create(CONTAINER_ID, new String[] {"sh", "-c", "id -u -n"})
                                                  .withDetach(false)))).thenReturn(getUserNameExecMock);
        doAnswer(invocation -> {
            String execId = ((StartExecParams)invocation.getArguments()[0]).getExecId();
            @SuppressWarnings("unchecked")
            MessageProcessor<LogMessage> messageProcessor = (MessageProcessor<LogMessage>)invocation.getArguments()[1];
            switch (execId) {
                case "getUserIdsExecMockId":
                    messageProcessor.process(new LogMessage(LogMessage.Type.STDOUT, USER_ID));
                    messageProcessor.process(new LogMessage(LogMessage.Type.STDOUT, USER_GID));
                    messageProcessor.process(new LogMessage(LogMessage.Type.STDOUT, USER_IN_CONTAINER));
                    break;
                case "getUserNameExecMockId":
                    messageProcessor.process(new LogMessage(LogMessage.Type.STDOUT, USER_IN_CONTAINER));
                    break;
                default:
                    throw new RuntimeException("Unexpected exec id");
            }

            return null;
        }).when(docker).startExec(any(StartExecParams.class), Matchers.any());

        executor = Executors.newFixedThreadPool(5);
    }

    @AfterMethod
    private void cleanup() {
        executor.shutdownNow();
    }

    @Test
    public void shouldBeAbleBackupWorkspace() throws Exception {
        injectWorkspaceLock(WORKSPACE_ID);

        backupManager.backupWorkspace(WORKSPACE_ID);

        verify(backupManager).executeCommand(cmdCaptor.capture(), eq(MAX_BACKUP_DURATION_SEC), eq(NODE_HOST));

        String[] command = cmdCaptor.getValue();
        assertArrayEquals(BACKUP_WORKSPACE_COMMAND, command);
    }

    @Test
    public void shouldNotBackupWorkspaceAfterBackupWithCleanup() throws Exception {
        injectWorkspaceLock(WORKSPACE_ID);
        backupManager.backupWorkspaceAndCleanup(WORKSPACE_ID,
                                                CONTAINER_ID,
                                                NODE_HOST);

        backupManager.backupWorkspace(WORKSPACE_ID);

        verify(backupManager).executeCommand(anyObject(), anyInt(), anyString());
    }

    @Test
    public void shouldNotBackupWSIfDevMachineInRuntimeDescriptorIsNull() throws Exception {
        injectWorkspaceLock(WORKSPACE_ID);
        when(workspaceRuntime.getDevMachine()).thenReturn(null);

        backupManager.backupWorkspace(WORKSPACE_ID);

        verify(backupManager, never()).executeCommand(any(String[].class), anyInt(), anyString());
        verifyNoMoreInteractions(docker,
                                 dockerInstance,
                                 dockerNode,
                                 machineRuntimeInfo,
                                 devMachine,
                                 workspaceFolderPathProvider,
                                 workspaceIdHashLocationFinder);
    }

    @Test(dataProvider = "nonRunningMachineStatusProvider")
    public void shouldNotBackupWSIfDevMachineStatusIsNotRunning(MachineStatus status) throws Exception {
        injectWorkspaceLock(WORKSPACE_ID);
        when(devMachine.getStatus()).thenReturn(status);

        backupManager.backupWorkspace(WORKSPACE_ID);

        verify(backupManager, never()).executeCommand(any(String[].class), anyInt(), anyString());
        verify(workspaceRuntimes).get(eq(WORKSPACE_ID));
        verifyNoMoreInteractions(docker,
                                 dockerInstance,
                                 dockerNode,
                                 workspaceRuntimes,
                                 machineRuntimeInfo,
                                 workspaceFolderPathProvider,
                                 workspaceIdHashLocationFinder);
    }

    @DataProvider(name = "nonRunningMachineStatusProvider")
    public static Object[][] nonRunningMachineStatusProvider() {
        return new Object[][] {
                {MachineStatus.CREATING},
                {MachineStatus.DESTROYING}
        };
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "test exception")
    public void shouldNotBackupWSIfDevMachineStatusIsNotFoundInWSRuntimes() throws Exception {
        injectWorkspaceLock(WORKSPACE_ID);
        when(workspaceRuntimes.getMachine(anyString(), anyString())).thenThrow(new NotFoundException("test exception"));

        backupManager.backupWorkspace(WORKSPACE_ID);

        verify(backupManager, never()).executeCommand(any(String[].class), anyInt(), anyString());
    }

    @Test
    public void shouldBeAbleBackupWorkspaceWithCleanup() throws Exception {
        injectWorkspaceLock(WORKSPACE_ID);
        backupManager.backupWorkspaceAndCleanup(WORKSPACE_ID,
                                                CONTAINER_ID,
                                                NODE_HOST);

        verify(backupManager).executeCommand(cmdCaptor.capture(), eq(MAX_BACKUP_DURATION_SEC), eq(NODE_HOST));

        String[] command = cmdCaptor.getValue();
        assertArrayEquals(BACKUP_WORKSPACE_WITH_CLEANUP_COMMAND, command);
    }

    @Test
    public void shouldBeAbleRestoreWorkspace() throws Exception {
        doNothing().when(backupManager).executeCommand(anyObject(), anyInt(), anyString());

        backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                             CONTAINER_ID,
                                             NODE_HOST);

        verify(backupManager).executeCommand(cmdCaptor.capture(), eq(MAX_RESTORE_DURATION_SEC), eq(NODE_HOST));

        String[] command = cmdCaptor.getValue();
        assertArrayEquals(RESTORE_WORKSPACE_COMMAND, command);
    }

    @Test
    public void shouldIgnoreNewBackupRequestIfPreviousOneHasBeenJustStarted() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // when
        backupManager.backupWorkspace(WORKSPACE_ID);

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).executeCommand(anyObject(), anyInt(), anyString());
    }

    @Test
    public void shouldIgnoreAllNewBackupRequestsIfOldHasNotFinishedYet() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // when
        executeTaskNTimesSimultaneouslyWithBarrier(this::runBackup);

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).executeCommand(anyObject(), anyInt(), anyString());
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Restore of workspace " + WORKSPACE_ID +
                                            " failed. Another restore process of the same workspace is in progress")
    public void throwsExceptionOnNewRestoreIfAnotherOneIsInProgress() throws Exception {
        // given
        ThreadFreezer restoreFreezer = startNewProcessAndFreeze(this::runRestore);

        // when
        try {
            // start another restore process
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                                 CONTAINER_ID,
                                                 NODE_HOST);
            fail("Second call of restore should throw an exception");
        } finally {
            // then
            restoreFreezer.unfreeze();
            awaitFinalization();
            verify(backupManager, times(1)).executeCommand(anyObject(), anyInt(), anyString());
        }
    }

    /**
     * Needed to ensure that failed extra restore doesn't unlock restoring
     */
    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Restore of workspace " + WORKSPACE_ID +
                                            " failed. Another restore process of the same workspace is in progress")
    public void throwsExceptionOnNewRestoreIfAnotherOneIsInProgressAndAnotherRestoreFailed() throws Exception {
        // given
        ThreadFreezer restoreFreezer = startNewProcessAndFreeze(this::runRestore);

        try {
            // start another restore process
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                                 CONTAINER_ID,
                                                 NODE_HOST);
            fail("Second call of restore should throw an exception");
        } catch (ServerException e) {
            assertEquals(e.getLocalizedMessage(), "Restore of workspace " + WORKSPACE_ID +
                                                  " failed. Another restore process of the same workspace is in progress");

            // when
            // start yet another restore process
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                                 CONTAINER_ID,
                                                 NODE_HOST);
            fail("Third call of restore should throw an exception");
        } finally {
            // complete waiting answer
            restoreFreezer.unfreeze();
            awaitFinalization();
            verify(backupManager, times(1)).executeCommand(anyObject(), anyInt(), anyString());
        }
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Restore of workspace " + WORKSPACE_ID +
                                            " failed. Another restore process of the same workspace is in progress")
    public void throwsExceptionOnNewRestoreAfterSuccessfulFirstOne() throws Exception {
        // given
        doNothing().when(backupManager).executeCommand(anyVararg(), anyInt(), anyString());

        // start restore process
        backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                             CONTAINER_ID,
                                             NODE_HOST);

        // when
        backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                             CONTAINER_ID,
                                             NODE_HOST);
    }

    /**
     * Needed to ensure that failed extra restore doesn't unlock restoring
     */
    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Restore of workspace " + WORKSPACE_ID +
                                            " failed. Another restore process of the same workspace is in progress")
    public void throwsExceptionAfterFailingRestoreThatFollowsSuccessfulOne() throws Exception {
        // given
        doNothing().when(backupManager).executeCommand(anyVararg(), anyInt(), anyString());

        // start restore process
        backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                             CONTAINER_ID,
                                             NODE_HOST);

        try {
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                                 CONTAINER_ID,
                                                 NODE_HOST);
        } catch (ServerException ignore) {}

        // when
        backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                             CONTAINER_ID,
                                             NODE_HOST);
    }

    @Test
    public void shouldThrowExceptionWhenStartRestoreIfBackupHasNotFinishedYet() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // when
        try {
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                                 CONTAINER_ID,
                                                 NODE_HOST);
            fail("Restore should not be performed while backup is in progress");
        } catch (ServerException ignore) {}

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).executeCommand(cmdCaptor.capture(), anyInt(), anyString());
        assertEquals(cmdCaptor.getValue()[0], BACKUP_SCRIPT);
    }

    @Test
    public void shouldIgnoreOthersBackupsAndFailOnRestoresWhileBackupInProgress() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // when
        executeTasksSimultaneouslyWithBarrier(this::runBackup,
                                              this::runRestore,
                                              this::runBackup,
                                              this::runRestore,
                                              this::runBackup,
                                              this::runRestore,
                                              this::runBackup,
                                              this::runRestore);

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).executeCommand(cmdCaptor.capture(), anyInt(), anyString());
        assertEquals(cmdCaptor.getValue()[0], BACKUP_SCRIPT);
    }


    @Test
    public void shouldIgnoreOthersBackupsAndFailOnRestoresWhileRestoreInProgress() throws Exception {
        // given
        ThreadFreezer restoreFreezer = startNewProcessAndFreeze(this::runRestore);

        // when
        executeTasksSimultaneouslyWithBarrier(this::runBackup,
                                              this::runRestore,
                                              this::runBackup,
                                              this::runRestore,
                                              this::runBackup,
                                              this::runRestore,
                                              this::runBackup,
                                              this::runRestore);

        restoreFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).executeCommand(cmdCaptor.capture(), anyInt(), anyString());
        assertEquals(cmdCaptor.getValue()[0], RESTORE_SCRIPT);
    }

    @Test
    public void shouldIgnoreBackupIfRestoreHasNotFinishedYet() throws Exception {
        // given
        ThreadFreezer restoreFreezer = startNewProcessAndFreeze(this::runRestore);

        // when
        backupManager.backupWorkspace(WORKSPACE_ID);

        restoreFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).executeCommand(cmdCaptor.capture(), anyInt(), anyString());
        assertEquals(cmdCaptor.getValue()[0], RESTORE_SCRIPT);
    }

    @Test
    public void shouldProcessOnlyOneBackupAtTheSameTime() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // when
        executeTaskNTimesSimultaneouslyWithBarrier(this::runBackup);

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).executeCommand(cmdCaptor.capture(), anyInt(), anyString());
        assertEquals(cmdCaptor.getValue()[0], BACKUP_SCRIPT);
    }

    @Test
    public void shouldBackupWithCleanupAfterFinishOfCurrentBackup() throws Exception {
        injectWorkspaceLock(WORKSPACE_ID);
        backupManager.backupWorkspace(WORKSPACE_ID);
        backupManager.backupWorkspaceAndCleanup(WORKSPACE_ID,
                                                CONTAINER_ID,
                                                NODE_HOST);
    }

    @Test
    public void shouldQueuedBackupWithCleanupAfterBackup() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // then
        executor.execute(this::runBackupWithCleanup);
        waitUntilWorkspaceLockLockedWithQueueLength(WORKSPACE_ID);

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(2)).executeCommand(cmdCaptor.capture(), anyInt(), anyString());
        assertEquals(cmdCaptor.getValue()[0], BACKUP_SCRIPT);
    }

    @Test
    public void shouldBackupWithCleanupAfterFinishOfCurrentRestore() throws Exception {
        backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                             CONTAINER_ID,
                                             NODE_HOST);
        backupManager.backupWorkspaceAndCleanup(WORKSPACE_ID,
                                                CONTAINER_ID,
                                                NODE_HOST);
        verify(backupManager, times(2)).executeCommand(anyObject(), anyInt(), eq(NODE_HOST));
    }

    @Test
    public void shouldBackupWithCleanupAfterFinishOfCurrentBackupButNotStartAnotherBackup()
            throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // then
        executor.execute(this::runBackupWithCleanup);
        waitUntilWorkspaceLockLockedWithQueueLength(WORKSPACE_ID);

        backupFreezer.unfreeze();
        awaitFinalization();

        backupManager.backupWorkspace(WORKSPACE_ID);

        verify(backupManager, times(2)).executeCommand(cmdCaptor.capture(), anyInt(), anyString());

        assertEquals(cmdCaptor.getValue()[0], BACKUP_SCRIPT);
    }

    @Test
    public void shouldIgnoreAllBackupsAndFailOnRestoresWhileBackupWithCleanupInQueueAfterBackup() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // when
        executor.execute(this::runBackupWithCleanup);
        waitUntilWorkspaceLockLockedWithQueueLength(WORKSPACE_ID);

        // after
        executeTasksSimultaneouslyWithBarrier(this::runBackup,
                                              this::runRestore,
                                              this::runBackup,
                                              this::runRestore,
                                              this::runBackup,
                                              this::runRestore,
                                              this::runBackup,
                                              this::runRestore);

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(2)).executeCommand(cmdCaptor.capture(), anyInt(), anyString());
        assertEquals(cmdCaptor.getValue()[0], BACKUP_SCRIPT);
    }

    /**
     * Runs processes to do specified task simultaneously and wait until they finish.
     *
     * @param task
     *        specifies task which will be run
     * @throws InterruptedException if waiting in this method is interrupted
     */
    private void executeTaskNTimesSimultaneouslyWithBarrier(Runnable task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            executor.execute(() -> {
                task.run();
                latch.countDown();
            });
        }

        latch.await();
    }

    /**
     * Runs given tasks simultaneously and wait until they finish.
     *
     * @param tasks
     *        tasks to run
     * @throws InterruptedException if waiting in this method is interrupted
     */
    private void executeTasksSimultaneouslyWithBarrier(Runnable ... tasks) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(tasks.length);

        for (Runnable task : tasks) {
            executor.execute(() -> {
                task.run();
                latch.countDown();
            });
        }

        latch.await();
    }

    private void runBackup() {
        try {
            backupManager.backupWorkspace(WORKSPACE_ID);
        } catch (ServerException | NotFoundException e) {
            LOG.error(e.getMessage());
        }
    }

    private void runBackupWithCleanup() {
        try {
            backupManager.backupWorkspaceAndCleanup(WORKSPACE_ID,
                                                    CONTAINER_ID,
                                                    NODE_HOST);
        } catch (ServerException e) {
            LOG.error(e.getMessage());
        }
    }

    private void runRestore() {
        try {
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID,
                                                 CONTAINER_ID,
                                                 NODE_HOST);
        } catch (ServerException e) {
            LOG.error(e.getMessage());
        }
    }

    private void awaitFinalization() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(1_000, TimeUnit.MILLISECONDS)) {
            fail("Operation is hanged up. Terminated.");
        }
    }

    /**
     * Adds lock for specified workspace into concurrent hash map of {@link DockerEnvironmentBackupManager} class
     * It allows emulate restore of workspace without invoking it explicit
     *
     * @param workspaceId
     *         id of workspace for which lock will be injected
     */
    private void injectWorkspaceLock(String workspaceId) {
        try {
            Field locks = DockerEnvironmentBackupManager.class.getDeclaredField("workspacesBackupLocks");
            locks.setAccessible(true);
            @SuppressWarnings("unchecked") // field workspacesBackupLocks newer change its type
            ConcurrentHashMap<String, ReentrantLock> workspacesBackupLocks =
                    (ConcurrentHashMap<String, ReentrantLock>)locks.get(backupManager);
            workspacesBackupLocks.put(workspaceId, new ReentrantLock());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Wait until lock of specified workspace will be locked.
     *
     * @param workspaceId
     *         workspace id to which loch belongs
     * @return true if lock is locked, false if lock doesn't exist
     */
    private boolean waitUntilWorkspaceLockLockedWithQueueLength(String workspaceId) {
        ReentrantLock lock;
        try {
            Field locks = DockerEnvironmentBackupManager.class.getDeclaredField("workspacesBackupLocks");
            locks.setAccessible(true);
            @SuppressWarnings("unchecked") // field workspacesBackupLocks newer change its type
            ConcurrentHashMap<String, ReentrantLock> workspacesBackupLocks =
                    (ConcurrentHashMap<String, ReentrantLock>)locks.get(backupManager);
            lock = workspacesBackupLocks.get(workspaceId);
            if (lock != null) {
                while (!lock.isLocked() || lock.getQueueLength() != 1) {
                    sleep(10);
                }
                return true;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException ignore) {
            // ok, exit
        }
        return false;
    }

    private ThreadFreezer startNewProcessAndFreeze(Runnable backupType) throws Exception {
        CountDownLatch releaseProcessLatch = new CountDownLatch(1);

        final CountDownLatch invokeProcessLatch = new CountDownLatch(1);
        doAnswer((invocationOnMock) -> {
            if (invokeProcessLatch.getCount() > 0) {
                invokeProcessLatch.countDown();
                releaseProcessLatch.await();
            }
            return null;
        }).when(backupManager).executeCommand(anyVararg(), anyInt(), anyString());

        executor.execute(backupType);

        invokeProcessLatch.await();
        doNothing().when(backupManager).executeCommand(anyVararg(), anyInt(), anyString());

        return new ThreadFreezer(releaseProcessLatch);
    }

    private static class ThreadFreezer {
        private CountDownLatch latch;

        ThreadFreezer(CountDownLatch latch) {
            if (latch.getCount() != 1) {
                throw new RuntimeException("Count down latch should have only 1 step to go");
            }
            this.latch = latch;
        }

        void unfreeze() {
            latch.countDown();
        }
    }
}
