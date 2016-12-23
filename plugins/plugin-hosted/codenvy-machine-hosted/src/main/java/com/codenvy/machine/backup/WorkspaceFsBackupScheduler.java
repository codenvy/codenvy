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
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Schedule backups of projects of running workspace.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class WorkspaceFsBackupScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceFsBackupScheduler.class);

    private final long                                  syncTimeoutMillisecond;
    private final WorkspaceRuntimes                     workspaceRuntimes;
    private final Map<String, Long>                     lastWorkspaceSynchronizationTime;
    private final ExecutorService                       executor;
    private final ConcurrentMap<String, String>         workspacesBackupsInProgress;
    private final Map<String, EnvironmentBackupManager> backupManagers;
    private final WorkspaceManager                      workspaceManager;

    @Inject
    public WorkspaceFsBackupScheduler(Map<String, EnvironmentBackupManager> backupManagers,
                                      WorkspaceRuntimes workspaceRuntimes,
                                      @Named("machine.backup.backup_period_second") long syncTimeoutSecond,
                                      WorkspaceManager workspaceManager) {
        this.workspaceRuntimes = workspaceRuntimes;
        this.backupManagers = backupManagers;
        this.syncTimeoutMillisecond = TimeUnit.SECONDS.toMillis(syncTimeoutSecond);
        this.workspaceManager = workspaceManager;

        this.executor = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("WorkspaceFsBackupScheduler-%s")
                                          .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                                          .build());
        this.lastWorkspaceSynchronizationTime = new ConcurrentHashMap<>();
        this.workspacesBackupsInProgress = new ConcurrentHashMap<>();
    }

    @ScheduleRate(initialDelay = 1, period = 1, unit = TimeUnit.MINUTES)
    public void scheduleBackup() {
        for (String workspaceId : workspaceRuntimes.getWorkspaces().keySet()) {

            try {
                // re-read workspace state to ensure that it's still active after processing of all previous workspaces
                WorkspaceImpl workspace = workspaceManager.getWorkspace(workspaceId);
                // get active env from this WS instead of wsStateEntry because
                // workspace could be restarted during backup of previous WSs with another environment
                WorkspaceRuntimeImpl runtime = workspace.getRuntime();
                // If workspace is not in RUNNING state skip it
                if (workspace.getStatus().equals(WorkspaceStatus.RUNNING)) {
                    if (isTimeToBackup(workspaceId)) {
                        executor.execute(() -> {
                            // don't start new backup if previous one is in progress
                            if (workspacesBackupsInProgress.putIfAbsent(workspaceId, workspaceId) == null) {
                                try {
                                    String environmentType = workspace.getConfig()
                                                                      .getEnvironments()
                                                                      .get(runtime.getActiveEnv())
                                                                      .getRecipe()
                                                                      .getType();
                                    EnvironmentBackupManager backupManager = backupManagers.get(environmentType);
                                    if (backupManager == null) {
                                        throw new ServerException("Backing up of environment of type " +
                                                                  environmentType + " is not implemented.");
                                    }

                                    backupManager.backupWorkspace(workspaceId);

                                    lastWorkspaceSynchronizationTime.put(workspaceId, System.currentTimeMillis());
                                } catch (NotFoundException ignore) {
                                    // it is ok, machine was stopped while this backup task was in the executor queue
                                } catch (Exception e) {
                                    LOG.error(e.getLocalizedMessage(), e);
                                } finally {
                                    workspacesBackupsInProgress.remove(workspaceId);
                                }
                            }
                        });
                    }
                }
            } catch (NotFoundException e) {
                // it's ok, means that ws is removed already
            } catch (ServerException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @VisibleForTesting
    boolean isTimeToBackup(String workspaceId) {
        final Long lastWorkspaceSyncTime = lastWorkspaceSynchronizationTime.get(workspaceId);

        return lastWorkspaceSyncTime == null ||
               System.currentTimeMillis() - lastWorkspaceSyncTime > syncTimeoutMillisecond;
    }

    @PreDestroy
    private void teardown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOG.warn("Unable terminate main pool");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
