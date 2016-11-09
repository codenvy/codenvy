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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import static com.codenvy.machine.agent.CodenvyInfrastructureProvisioner.SYNC_STRATEGY_PROPERTY;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Copies workspace files between machine's host and backup storage.
 *
 * @author Alexander Garagatyi
 * @author Mykola Morhun
 */
@Singleton
public class MachineBackupManager {
    private static final Logger LOG = getLogger(MachineBackupManager.class);

    private final String                               backupScript;
    private final String                               restoreScript;
    private final int                                  maxBackupDuration;
    private final int                                  restoreDuration;
    private final File                                 backupsRootDir;
    private final WorkspaceIdHashLocationFinder        workspaceIdHashLocationFinder;
    private final String                               projectFolderPath;
    private final ConcurrentMap<String, ReentrantLock> workspacesBackupLocks;
    private final boolean                              syncAgentInMachine;

    @Inject
    public MachineBackupManager(@Named("machine.backup.backup_script") String backupScript,
                                @Named("machine.backup.restore_script") String restoreScript,
                                @Named("machine.backup.backup_duration_second") int maxBackupDurationSec,
                                @Named("machine.backup.restore_duration_second") int restoreDurationSec,
                                @Named("che.user.workspaces.storage") File backupsRootDir,
                                WorkspaceIdHashLocationFinder workspaceIdHashLocationFinder,
                                @Named(SYNC_STRATEGY_PROPERTY) String syncStrategy,
                                @Named("che.workspace.projects.storage") String projectFolderPath) {
        this.backupScript = backupScript;
        this.restoreScript = restoreScript;
        this.maxBackupDuration = maxBackupDurationSec;
        this.restoreDuration = restoreDurationSec;
        this.backupsRootDir = backupsRootDir;
        this.workspaceIdHashLocationFinder = workspaceIdHashLocationFinder;
        this.projectFolderPath = projectFolderPath;

        switch (syncStrategy) {
            case "rsync":
                syncAgentInMachine = false;
                break;
            case "rsync-agent":
                syncAgentInMachine = true;
                break;
            default:
                throw new RuntimeException(
                        format("Property '%s' has illegal value '%s'. Valid values: rsync, rsync-agent",
                               SYNC_STRATEGY_PROPERTY,
                               syncStrategy));
        }

        workspacesBackupLocks = new ConcurrentHashMap<>();
    }

    /**
     * Copies workspace files from machine's host to backup storage.
     *
     * @param workspaceId
     *         id of workspace that should be backed up
     * @param srcPath
     *         path to folder that should be backed up
     * @param srcAddress
     *         address of the server from which workspace files should be backed up
     * @param srcPort
     *         port on the server that should be used for workspace files backup connection
     */
    public void backupWorkspace(String workspaceId,
                                String srcPath,
                                String srcAddress,
                                int srcPort) throws ServerException {
        ReentrantLock lock = workspacesBackupLocks.get(workspaceId);
        // backup workspace only if no backup with cleanup before
        if (lock != null) {
            // backup workspace only if this workspace isn't under backup/restore process
            if (lock.tryLock()) {
                try {
                    if (workspacesBackupLocks.get(workspaceId) == null) {
                        // It is possible to reach here, because remove lock from locks map and following unlock in
                        // backup with cleanup method is not atomic operation.
                        // In very rare case it may happens, but it is ok. Just ignore this backup
                        // because it is called after cleanup
                        return;
                    }
                    backupWorkspace(workspaceId,
                                    srcPath,
                                    srcAddress,
                                    srcPort,
                                    false);
                } finally {
                    lock.unlock();
                }
            }
        } else {
            LOG.warn("Attempt to backup workspace {} after cleanup", workspaceId);
        }
    }

    /**
     * Copies workspace files from machine's host to backup storage and remove all files from the source.
     *
     * @param workspaceId
     *         id of workspace that should be backed up
     * @param srcPath
     *         path to folder that should be backed up
     * @param srcAddress
     *         address of the server from which workspace files should be backed up
     * @param srcPort
     *         port on the server that should be used for workspace files backup connection
     */
    public void backupWorkspaceAndCleanup(String workspaceId,
                                          String srcPath,
                                          String srcAddress,
                                          int srcPort) throws ServerException {
        ReentrantLock lock = workspacesBackupLocks.get(workspaceId);
        if (lock != null) {
            lock.lock();
            try {
                if (workspacesBackupLocks.get(workspaceId) == null) {
                    // it is possible to reach here if invoke this method again while previous one is in progress
                    LOG.error("Backup with cleanup of the workspace {} was invoked several times simultaneously", workspaceId);
                    return;
                }
                backupWorkspace(workspaceId,
                                srcPath,
                                srcAddress,
                                srcPort,
                                true);
            } finally {
                workspacesBackupLocks.remove(workspaceId);
                lock.unlock();
            }
        } else {
            LOG.warn("Attempt to backup workspace {} after cleanup", workspaceId);
        }
    }

    @VisibleForTesting
    void backupWorkspace(String workspaceId,
                         String srcPath,
                         String srcAddress,
                         int srcPort,
                         boolean removeSourceOnSuccess) throws ServerException {
        if (syncAgentInMachine) {
            srcPath = projectFolderPath;
        }
        String destPath = workspaceIdHashLocationFinder.calculateDirPath(backupsRootDir, workspaceId).toString();

        CommandLine commandLine = new CommandLine(backupScript,
                                                  srcPath,
                                                  srcAddress,
                                                  Integer.toString(srcPort),
                                                  destPath,
                                                  Boolean.toString(removeSourceOnSuccess));

        try {
            execute(commandLine.asArray(), maxBackupDuration);
        } catch (TimeoutException e) {
            throw new ServerException("Backup of workspace " + workspaceId + " filesystem terminated due to timeout on "
                                      + srcAddress + " node.");
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Backup of workspace " + workspaceId + " filesystem interrupted on " + srcAddress + " node.");
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Backup of workspace " + workspaceId + " filesystem terminated on " + srcAddress + " node. "
                                      + e.getLocalizedMessage());
        }
    }

    /**
     * Synchronously copies workspace files from backup storage to machine's host.
     *
     * @param workspaceId
     *         id of workspace that should be copied to machine
     * @param destinationPath
     *         path to folder that should be restored from backup
     * @param userId
     *         ID of user to apply permission to files on restoring
     * @param groupId
     *         ID of user group to apply permission to files on restoring
     * @param destAddress
     *         address of a server where workspace files should be restored
     * @param destPort
     *         port of a server where workspace files should be restored
     * @throws ServerException
     *         if any exception occurs
     */
    public void restoreWorkspaceBackup(String workspaceId,
                                       String destinationPath,
                                       String userId,
                                       String groupId,
                                       String destAddress,
                                       int destPort) throws ServerException {
        boolean restored = false;
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try {
            if (workspacesBackupLocks.putIfAbsent(workspaceId, lock) != null) {
                // it shouldn't happen, but for case when restore of one workspace is invoked simultaneously
                String err = "Restore of workspace " + workspaceId +
                             " failed. Another restore process of the same workspace is in progress";
                LOG.error(err);
                throw new ServerException(err);
            }
            String srcPath = workspaceIdHashLocationFinder.calculateDirPath(backupsRootDir, workspaceId).toString();
            if (syncAgentInMachine) {
                destinationPath = projectFolderPath;
            }

            Files.createDirectories(Paths.get(srcPath));

            CommandLine commandLine = new CommandLine(restoreScript,
                                                      srcPath,
                                                      destinationPath,
                                                      destAddress,
                                                      Integer.toString(destPort),
                                                      userId,
                                                      groupId);

            execute(commandLine.asArray(), restoreDuration);
            restored = true;
        } catch (TimeoutException e) {
            throw new ServerException("Restoring of workspace " + workspaceId + " filesystem terminated due to timeout on "
                                      + destAddress + " node.");
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Restoring of workspace " + workspaceId + " filesystem interrupted on " + destAddress + " node.");
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Restoring of workspace " + workspaceId + " filesystem terminated on " + destAddress + " node. "
                                      + e.getLocalizedMessage());
        } finally {
            lock.unlock();
            if (!restored) {
                workspacesBackupLocks.remove(workspaceId, lock);
            }
        }
    }

    @VisibleForTesting
    void execute(String[] commandLine, int timeout) throws TimeoutException, IOException, InterruptedException {
        final ListLineConsumer outputConsumer = new ListLineConsumer();
        Process process = ProcessUtil.executeAndWait(commandLine, timeout, SECONDS, outputConsumer);

        if (process.exitValue() != 0) {
            LOG.error(outputConsumer.getText());
            throw new IOException("Process failed. Exit code " + process.exitValue());
        }
    }
}
