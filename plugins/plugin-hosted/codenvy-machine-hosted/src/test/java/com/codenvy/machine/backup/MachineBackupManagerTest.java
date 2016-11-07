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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

/**
 * @author Mykola Morhun
 */
@Listeners(value = {MockitoTestNGListener.class})
public class MachineBackupManagerTest {
    private static final Logger LOG = getLogger(MachineBackupManagerTest.class);

    private static final String WORKSPACE_ID                   = "workspaceId";
    private static final String BACKUP_SCRIPT                  = "/tmp/backup.sh";
    private static final String RESTORE_SCRIPT                 = "/tmp/restore.sh";
    private static final int    MAX_BACKUP_DURATION_SEC        = 10;
    private static final int    MAX_RESTORE_DURATION_SEC       = 10;
    private static final String BACKUPS_ROOT_PATH              = "/tmp/che/backups";
    private static final String SRC_PATH                       = "srcPath";
    private static final String SRC_ADDRESS                    = "srcAddress";
    private static final String DEST_PATH                      = "/tmp/restore.sh";
    private static final String DEST_ADDRESS                   = "/tmp/restore.sh";
    private static final String USER_ID                        = "1000";
    private static final String USER_GID                       = "1000";
    private static final String PATH_TO_WORKSPACE              = "00/00/00/";
    private static final String ABSOLUTE_PATH_TO_WORKSPACE_DIR = BACKUPS_ROOT_PATH + PATH_TO_WORKSPACE + WORKSPACE_ID;

    private static final String[] BACKUP_WORKSPACE_COMMAND              =
            {BACKUP_SCRIPT, SRC_PATH, SRC_ADDRESS, ABSOLUTE_PATH_TO_WORKSPACE_DIR, "false"};
    private static final String[] BACKUP_WORKSPACE_WITH_CLEANUP_COMMAND =
            {BACKUP_SCRIPT, SRC_PATH, SRC_ADDRESS, ABSOLUTE_PATH_TO_WORKSPACE_DIR, "true"};
    private static final String[] RESTORE_WORKSPACE_COMMAND             =
            {RESTORE_SCRIPT, ABSOLUTE_PATH_TO_WORKSPACE_DIR, RESTORE_SCRIPT, RESTORE_SCRIPT, USER_ID, USER_GID};

    @Mock
    private WorkspaceIdHashLocationFinder workspaceIdHashLocationFinder;

    @Captor
    private ArgumentCaptor<String[]> cmdCaptor;

    private ExecutorService executor;

    private MachineBackupManager backupManager;

    @BeforeMethod
    private void setup() throws ServerException, InterruptedException, IOException, TimeoutException {
        backupManager = spy(new MachineBackupManager(BACKUP_SCRIPT,
                                                     RESTORE_SCRIPT,
                                                     MAX_BACKUP_DURATION_SEC,
                                                     MAX_RESTORE_DURATION_SEC,
                                                     new File(BACKUPS_ROOT_PATH),
                                                     workspaceIdHashLocationFinder));

        when(workspaceIdHashLocationFinder.calculateDirPath(any(File.class), any(String.class)))
                .thenReturn(new File(ABSOLUTE_PATH_TO_WORKSPACE_DIR));

        doNothing().when(backupManager).execute(anyObject(), anyInt());

        executor = Executors.newFixedThreadPool(5);
    }

    @AfterMethod
    private void cleanup() {
        executor.shutdownNow();
    }

    @Test
    public void shouldBeAbleBackupWorkspace() throws Exception {
        injectWorkspaceLock(WORKSPACE_ID);

        backupManager.backupWorkspace(WORKSPACE_ID, SRC_PATH, SRC_ADDRESS, "");

        verify(backupManager).execute(cmdCaptor.capture(), eq(MAX_BACKUP_DURATION_SEC));

        String[] command = cmdCaptor.getValue();
        assertArrayEquals(BACKUP_WORKSPACE_COMMAND, command);
    }

    @Test
    public void shouldNotBackupWorkspaceAfterBackupWithCleanup() throws Exception {
        injectWorkspaceLock(WORKSPACE_ID);
        backupManager.backupWorkspaceAndCleanup(WORKSPACE_ID, SRC_ADDRESS, "");

        backupManager.backupWorkspace(WORKSPACE_ID, SRC_PATH, SRC_ADDRESS, "");

        verify(backupManager).execute(anyObject(), anyInt());
    }

    @Test
    public void shouldBeAbleBackupWorkspaceWithCleanup() throws Exception {
        injectWorkspaceLock(WORKSPACE_ID);
        backupManager.backupWorkspaceAndCleanup(WORKSPACE_ID, SRC_ADDRESS, "");

        verify(backupManager).execute(cmdCaptor.capture(), eq(MAX_BACKUP_DURATION_SEC));

        String[] command = cmdCaptor.getValue();
        assertArrayEquals(BACKUP_WORKSPACE_WITH_CLEANUP_COMMAND, command);
    }

    @Test
    public void shouldBeAbleRestoreWorkspace() throws ServerException, InterruptedException, IOException, TimeoutException {
        doNothing().when(backupManager).execute(anyObject(), anyInt());

        backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");

        verify(backupManager).execute(cmdCaptor.capture(), eq(MAX_RESTORE_DURATION_SEC));

        String[] command = cmdCaptor.getValue();
        assertArrayEquals(RESTORE_WORKSPACE_COMMAND, command);
    }

    @Test
    public void shouldIgnoreNewBackupRequestIfPreviousOneHasBeenJustStarted() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // when
        backupManager.backupWorkspace(WORKSPACE_ID, SRC_PATH, SRC_ADDRESS, "");

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).execute(anyObject(), anyInt());
    }

    @Test
    public void shouldIgnoreAllNewBackupRequestsIfOldHasNotFinishedYet() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // when
        executeTaskNTimesSimultaneouslyWithBarrier(this::runBackup, 5);

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).execute(anyObject(), anyInt());
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
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");
            fail("Second call of restore should throw an exception");
        } finally {
            // then
            restoreFreezer.unfreeze();
            awaitFinalization();
            verify(backupManager, times(1)).execute(anyObject(), anyInt());
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
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");
            fail("Second call of restore should throw an exception");
        } catch (ServerException e) {
            assertEquals(e.getLocalizedMessage(), "Restore of workspace " + WORKSPACE_ID +
                                                  " failed. Another restore process of the same workspace is in progress");

            // when
            // start yet another restore process
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");
            fail("Third call of restore should throw an exception");
        } finally {
            // complete waiting answer
            restoreFreezer.unfreeze();
            awaitFinalization();
            verify(backupManager, times(1)).execute(anyObject(), anyInt());
        }
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Restore of workspace " + WORKSPACE_ID +
                                            " failed. Another restore process of the same workspace is in progress")
    public void throwsExceptionOnNewRestoreAfterSuccessfulFirstOne() throws Exception {
        // given
        doNothing().when(backupManager).execute(anyVararg(), anyInt());

        // start restore process
        backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");

        // when
        backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");
    }

    /**
     * Needed to ensure that failed extra restore doesn't unlock restoring
     */
    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Restore of workspace " + WORKSPACE_ID +
                                            " failed. Another restore process of the same workspace is in progress")
    public void throwsExceptionAfterFailingRestoreThatFollowsSuccessfulOne() throws Exception {
        // given
        doNothing().when(backupManager).execute(anyVararg(), anyInt());

        // start restore process
        backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");

        try {
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");
        } catch (ServerException ignore) {}

        // when
        backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");
    }

    @Test
    public void shouldThrowExceptionWhenStartRestoreIfBackupHasNotFinishedYet() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // when
        try {
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");
            fail("Restore should not be performed while backup is in progress");
        } catch (ServerException ignore) {}

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).execute(cmdCaptor.capture(), anyInt());
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
        verify(backupManager, times(1)).execute(cmdCaptor.capture(), anyInt());
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
        verify(backupManager, times(1)).execute(cmdCaptor.capture(), anyInt());
        assertEquals(cmdCaptor.getValue()[0], RESTORE_SCRIPT);
    }

    @Test
    public void shouldIgnoreBackupIfRestoreHasNotFinishedYet() throws Exception {
        // given
        ThreadFreezer restoreFreezer = startNewProcessAndFreeze(this::runRestore);

        // when
        backupManager.backupWorkspace(WORKSPACE_ID, SRC_PATH, SRC_ADDRESS, "");

        restoreFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).execute(cmdCaptor.capture(), anyInt());
        assertEquals(cmdCaptor.getValue()[0], RESTORE_SCRIPT);
    }

    @Test
    public void shouldProcessOnlyOneBackupAtTheSameTime() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // when
        executeTaskNTimesSimultaneouslyWithBarrier(this::runBackup, 5);

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(1)).execute(cmdCaptor.capture(), anyInt());
        assertEquals(cmdCaptor.getValue()[0], BACKUP_SCRIPT);
    }

    @Test
    public void shouldBackupWithCleanupAfterFinishOfCurrentBackup() throws Exception {
        injectWorkspaceLock(WORKSPACE_ID);
        backupManager.backupWorkspace(WORKSPACE_ID, SRC_PATH, SRC_ADDRESS, "");
        backupManager.backupWorkspaceAndCleanup(WORKSPACE_ID, SRC_ADDRESS, "");
    }

    @Test
    public void shouldQueuedBackupWithCleanupAfterBackup() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // then
        executor.execute(this::runBackupWithCleanup);
        waitUntilWorkspaceLockLockedWithQueueLength(WORKSPACE_ID, 1);

        backupFreezer.unfreeze();
        awaitFinalization();

        // then
        verify(backupManager, times(2)).execute(cmdCaptor.capture(), anyInt());
        assertEquals(cmdCaptor.getValue()[0], BACKUP_SCRIPT);
    }

    @Test
    public void shouldBackupWithCleanupAfterFinishOfCurrentRestore() throws Exception {
        backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");
        backupManager.backupWorkspaceAndCleanup(WORKSPACE_ID, SRC_ADDRESS, "");
        verify(backupManager, times(2)).execute(anyObject(), anyInt());
    }

    @Test
    public void shouldBackupWithCleanupAfterFinishOfCurrentBackupButNotStartAnotherBackup() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // then
        executor.execute(this::runBackupWithCleanup);
        waitUntilWorkspaceLockLockedWithQueueLength(WORKSPACE_ID, 1);

        backupFreezer.unfreeze();
        awaitFinalization();

        backupManager.backupWorkspace(WORKSPACE_ID, SRC_PATH, SRC_ADDRESS, "");

        verify(backupManager, times(2)).execute(cmdCaptor.capture(), anyInt());

        assertEquals(cmdCaptor.getValue()[0], BACKUP_SCRIPT);
    }

    @Test
    public void shouldIgnoreAllBackupsAndFailOnRestoresWhileBackupWithCleanupInQueueAfterBackup() throws Exception {
        // given
        injectWorkspaceLock(WORKSPACE_ID);
        ThreadFreezer backupFreezer = startNewProcessAndFreeze(this::runBackup);

        // when
        executor.execute(this::runBackupWithCleanup);
        waitUntilWorkspaceLockLockedWithQueueLength(WORKSPACE_ID, 1);

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
        verify(backupManager, times(2)).execute(cmdCaptor.capture(), anyInt());
        assertEquals(cmdCaptor.getValue()[0], BACKUP_SCRIPT);
    }

    /**
     * Runs processes to do specified task simultaneously and wait until they finish.
     *
     * @param task
     *        specifies task which will be run
     * @param times
     *         number of parallel jobs with specified task
     * @throws InterruptedException
     */
    private void executeTaskNTimesSimultaneouslyWithBarrier(Runnable task, int times) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(times);

        for (int i = 0; i < times; i++) {
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
     * @throws InterruptedException
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
            backupManager.backupWorkspace(WORKSPACE_ID, SRC_PATH, SRC_ADDRESS, "");
        } catch (ServerException e) {
            LOG.error(e.getMessage());
        }
    }

    private void runBackupWithCleanup() {
        try {
            backupManager.backupWorkspaceAndCleanup(WORKSPACE_ID, SRC_ADDRESS, "");
        } catch (ServerException e) {
            LOG.error(e.getMessage());
        }
    }

    private void runRestore() {
        try {
            backupManager.restoreWorkspaceBackup(WORKSPACE_ID, USER_ID, USER_GID, DEST_ADDRESS, "");
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
     * Adds lock for specified workspace into concurrent hash map of {@link MachineBackupManager} class
     * It allows emulate restore of workspace without invoking it explicit
     *
     * @param workspaceId
     *         id of workspace for which lock will be injected
     */
    private void injectWorkspaceLock(String workspaceId) {
        try {
            Field locks = MachineBackupManager.class.getDeclaredField("workspacesBackupLocks");
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
     * @param queueLength
     *         length of queue for lock workspace's lock
     * @return true if lock is locked, false if lock doesn't exist
     */
    private boolean waitUntilWorkspaceLockLockedWithQueueLength(String workspaceId, int queueLength) {
        ReentrantLock lock;
        try {
            Field locks = MachineBackupManager.class.getDeclaredField("workspacesBackupLocks");
            locks.setAccessible(true);
            @SuppressWarnings("unchecked") // field workspacesBackupLocks newer change its type
            ConcurrentHashMap<String, ReentrantLock> workspacesBackupLocks =
                    (ConcurrentHashMap<String, ReentrantLock>)locks.get(backupManager);
            lock = workspacesBackupLocks.get(workspaceId);
            if (lock != null) {
                while (!lock.isLocked() || lock.getQueueLength() != queueLength) {
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
        }).when(backupManager).execute(anyVararg(), anyInt());

        executor.execute(backupType);

        invokeProcessLatch.await();
        doNothing().when(backupManager).execute(anyVararg(), anyInt());

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
