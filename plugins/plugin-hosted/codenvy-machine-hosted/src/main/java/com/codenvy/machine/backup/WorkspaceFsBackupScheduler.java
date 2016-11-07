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
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceNode;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
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
 * Schedule backups of workspace fs of running machines.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class WorkspaceFsBackupScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceFsBackupScheduler.class);

    private final long                 syncTimeoutMillisecond;
    private final WorkspaceRuntimes    workspaceRuntimes;
    private final MachineBackupManager backupManager;

    private final Map<String, Long>             lastMachineSynchronizationTime;
    private final ExecutorService               executor;
    private final ConcurrentMap<String, String> devMachinesBackupsInProgress;

    @Inject
    public WorkspaceFsBackupScheduler(WorkspaceRuntimes workspaceRuntimes,
                                      MachineBackupManager backupManager,
                                      @Named("machine.backup.backup_period_second") long syncTimeoutSecond) {
        this.workspaceRuntimes = workspaceRuntimes;
        this.backupManager = backupManager;
        this.syncTimeoutMillisecond = TimeUnit.SECONDS.toMillis(syncTimeoutSecond);

        this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("MachineFsBackupScheduler-%s").build());
        this.lastMachineSynchronizationTime = new ConcurrentHashMap<>();
        this.devMachinesBackupsInProgress = new ConcurrentHashMap<>();
    }

    @ScheduleRate(initialDelay = 1, period = 1, unit = TimeUnit.MINUTES)
    public void scheduleBackup() {
        for (Map.Entry<String, WorkspaceRuntimes.WorkspaceState> wsStateEntry :
                workspaceRuntimes.getWorkspaces().entrySet()) {

            try {
                WorkspaceRuntimes.RuntimeDescriptor runtimeDescriptor = workspaceRuntimes.get(wsStateEntry.getKey());
                if (runtimeDescriptor.getRuntimeStatus().equals(WorkspaceStatus.RUNNING)) {

                    MachineImpl devMachine = runtimeDescriptor.getRuntime().getDevMachine();
                    String machineId = devMachine.getId();

                    if (isTimeToBackup(machineId)) {
                        executor.execute(() -> {
                            // don't start new backup if previous one is in progress
                            if (devMachinesBackupsInProgress.putIfAbsent(machineId, machineId) == null) {
                                try {
                                    backupWorkspaceInMachine(devMachine);

                                    lastMachineSynchronizationTime.put(machineId, System.currentTimeMillis());
                                } catch (NotFoundException ignore) {
                                    // it is ok, machine was stopped while this backup task was in the executor queue
                                } catch (ServerException e) {
                                    LOG.error(e.getLocalizedMessage(), e);
                                } finally {
                                    devMachinesBackupsInProgress.remove(machineId);
                                }
                            }
                        });
                    }
                }
            } catch (NotFoundException e) {
                // it's ok, means that ws is stopped
            } catch (ServerException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @VisibleForTesting
    void backupWorkspaceInMachine(MachineImpl machine) throws NotFoundException, ServerException {
        final Instance machineInstance = workspaceRuntimes.getMachine(machine.getWorkspaceId(), machine.getId());
        // for case if this task is in the executor queue and user stopped this machine before execution
        if (machineInstance.getStatus() != MachineStatus.RUNNING) {
            return;
        }
        final InstanceNode node = machineInstance.getNode();

        // TODO what to do here?
        ServerImpl server = machine.getRuntime().getServers().get("22/tcp");
        if (server == null) {
            throw new ServerException("");
        }


        backupManager.backupWorkspace(machine.getWorkspaceId(),
                                      node.getProjectsFolder(),
                                      node.getHost(),
                                      server.getAddress().split(":", 2)[1]);
    }

    @VisibleForTesting
    boolean isTimeToBackup(String machineId) {
        final Long lastMachineSyncTime = lastMachineSynchronizationTime.get(machineId);

        return lastMachineSyncTime == null || System.currentTimeMillis() - lastMachineSyncTime > syncTimeoutMillisecond;
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
