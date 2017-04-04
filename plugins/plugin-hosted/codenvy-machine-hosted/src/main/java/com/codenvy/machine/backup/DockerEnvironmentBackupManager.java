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
package com.codenvy.machine.backup;

import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.plugin.docker.machine.DockerInstance;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Copies workspace files between docker machine and backup storage.
 *
 * @author Alexander Garagatyi
 * @author Mykola Morhun
 */
@Singleton
public class DockerEnvironmentBackupManager implements EnvironmentBackupManager {
    private static final Logger LOG                  = getLogger(DockerEnvironmentBackupManager.class);
    private static final String ERROR_MESSAGE_PREFIX =
            "Can't detect container user ids to chown backed up files of workspace ";

    private final String                               backupScript;
    private final String                               restoreScript;
    private final int                                  maxBackupDuration;
    private final int                                  restoreDuration;
    private final File                                 backupsRootDir;
    private final WorkspaceIdHashLocationFinder        workspaceIdHashLocationFinder;
    private final String                               projectFolderPath;
    private final ConcurrentMap<String, ReentrantLock> workspacesBackupLocks;
    private final WorkspaceManager                     workspaceManager;
    private final DockerConnector                      dockerConnector;

    @Inject
    public DockerEnvironmentBackupManager(@Named("machine.backup.backup_script") String backupScript,
                                          @Named("machine.backup.restore_script") String restoreScript,
                                          @Named("machine.backup.backup_duration_second") int maxBackupDurationSec,
                                          @Named("machine.backup.restore_duration_second") int restoreDurationSec,
                                          @Named("che.user.workspaces.storage") File backupsRootDir,
                                          WorkspaceIdHashLocationFinder workspaceIdHashLocationFinder,
                                          @Named("che.workspace.projects.storage") String projectFolderPath,
                                          WorkspaceManager workspaceManager,
                                          DockerConnector dockerConnector) {
        this.backupScript = backupScript;
        this.restoreScript = restoreScript;
        this.maxBackupDuration = maxBackupDurationSec;
        this.restoreDuration = restoreDurationSec;
        this.backupsRootDir = backupsRootDir;
        this.workspaceIdHashLocationFinder = workspaceIdHashLocationFinder;
        this.projectFolderPath = projectFolderPath;
        this.workspaceManager = workspaceManager;
        this.dockerConnector = dockerConnector;

        workspacesBackupLocks = new ConcurrentHashMap<>();
    }

    @Override
    public void backupWorkspace(String workspaceId) throws ServerException, NotFoundException {
        try {
            WorkspaceImpl workspace = workspaceManager.getWorkspace(workspaceId);
            if (workspace.getRuntime() == null) {
                throw new NotFoundException("Workspace is not running");
            }
            Machine devMachine = workspace.getRuntime().getDevMachine();
            if (devMachine == null || devMachine.getStatus() != MachineStatus.RUNNING) {
                // may happen if WS is no longer in RUNNING state
                return;
            }
            // machine that is not in running state can be just a stub and should not be casted
            DockerInstance dockerDevMachine = (DockerInstance)workspaceManager.getMachineInstance(workspaceId,
                                                                                                  devMachine.getId());
            String nodeHost = dockerDevMachine.getNode().getHost();
            String destPath = workspaceIdHashLocationFinder.calculateDirPath(backupsRootDir, workspaceId).toString();
            String srcUserName = getUserName(workspaceId, dockerDevMachine.getContainer()).name;
            int syncPort = getSyncPort(dockerDevMachine);

            backupInsideLock(workspaceId,
                             projectFolderPath,
                             nodeHost,
                             syncPort,
                             srcUserName,
                             destPath);
        } catch (IOException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Copy files of workspace into backup storage and cleanup them in container.
     *
     * @param workspaceId
     *         id of workspace to backup
     * @param containerId
     *         id of container that contains data
     * @param nodeHost
     *         host of a node where container is running
     * @throws ServerException
     *         if any error occurs
     */
    public void backupWorkspaceAndCleanup(String workspaceId,
                                          String containerId,
                                          String nodeHost) throws ServerException {
        try {
            String destPath = workspaceIdHashLocationFinder.calculateDirPath(backupsRootDir, workspaceId).toString();
            // if sync agent is not in machine port parameter is not used
            int syncPort = getSyncPort(containerId);
            String srcUserName = getUserName(workspaceId, containerId).name;

            backupAndCleanupInsideLock(workspaceId,
                                       projectFolderPath,
                                       nodeHost,
                                       syncPort,
                                       srcUserName,
                                       destPath);
        } catch (IOException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        } finally {
            // remove lock in case exception prevent removing it in regular place to prevent resources leak
            // and blocking further WS start
            workspacesBackupLocks.remove(workspaceId);
        }
    }

    /**
     * Copy files of workspace from backups storage into container.
     *
     * @param workspaceId
     *         id of workspace to backup
     * @param containerId
     *         id of container where data should be copied
     * @param nodeHost
     *         host of a node where container is running
     * @throws ServerException
     *         if any error occurs
     */
    public void restoreWorkspaceBackup(String workspaceId,
                                       String containerId,
                                       String nodeHost) throws ServerException {
        try {
            String srcPath = workspaceIdHashLocationFinder.calculateDirPath(backupsRootDir, workspaceId).toString();
            User user = getUserNameAndIds(workspaceId, containerId);
            int syncPort = getSyncPort(containerId);

            restoreBackupInsideLock(workspaceId,
                                    srcPath,
                                    projectFolderPath,
                                    user.id,
                                    user.groupId,
                                    user.name,
                                    nodeHost,
                                    syncPort);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(format("Can't restore file system of workspace %s in container %s",
                                             workspaceId, containerId));
        }
    }

    private void backupInsideLock(String workspaceId,
                                  String srcPath,
                                  String srcAddress,
                                  int srcPort,
                                  String srcUserName,
                                  String destPath) throws ServerException {
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
                    executeBackupScript(workspaceId,
                                        srcPath,
                                        srcAddress,
                                        srcPort,
                                        false,
                                        srcUserName,
                                        destPath);
                } finally {
                    lock.unlock();
                }
            }
        } else {
            LOG.warn("Attempt to backup workspace {} after cleanup", workspaceId);
        }
    }

    private void backupAndCleanupInsideLock(String workspaceId,
                                            String srcPath,
                                            String srcAddress,
                                            int srcPort,
                                            String srcUserName,
                                            String destPath) throws ServerException {
        ReentrantLock lock = workspacesBackupLocks.get(workspaceId);
        if (lock != null) {
            lock.lock();
            try {
                if (workspacesBackupLocks.get(workspaceId) == null) {
                    // it is possible to reach here if invoke this method again while previous one is in progress
                    LOG.error("Backup with cleanup of the workspace {} was invoked several times simultaneously",
                              workspaceId);
                    return;
                }
                executeBackupScript(workspaceId,
                                    srcPath,
                                    srcAddress,
                                    srcPort,
                                    true,
                                    srcUserName,
                                    destPath);
            } finally {
                workspacesBackupLocks.remove(workspaceId);
                lock.unlock();
            }
        } else {
            LOG.warn("Attempt to backup workspace {} after cleanup", workspaceId);
        }
    }

    private void restoreBackupInsideLock(String workspaceId,
                                         String srcPath,
                                         String destinationPath,
                                         String destUserId,
                                         String destGroupId,
                                         String destUserName,
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

            // TODO refactor that code to eliminate creation of directories here
            Files.createDirectories(Paths.get(srcPath));

            CommandLine commandLine = new CommandLine(restoreScript,
                                                      srcPath,
                                                      destinationPath,
                                                      destAddress,
                                                      Integer.toString(destPort),
                                                      destUserId,
                                                      destGroupId,
                                                      destUserName);

            executeCommand(commandLine.asArray(), restoreDuration, destAddress, workspaceId);
            restored = true;
        } catch (TimeoutException e) {
            throw new ServerException(
                    "Restoring of workspace " + workspaceId + " filesystem terminated due to timeout on "
                    + destAddress + " node.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerException(
                    "Restoring of workspace " + workspaceId + " filesystem interrupted on " + destAddress + " node.");
        } catch (IOException e) {
            String error = "Restoring of workspace " + workspaceId +
                           " filesystem terminated on " + destAddress + " node. "
                           + e.getLocalizedMessage();
            LOG.error(error, e);
            throw new ServerException(error);
        } finally {
            lock.unlock();
            if (!restored) {
                workspacesBackupLocks.remove(workspaceId, lock);
            }
        }
    }

    private void executeBackupScript(String workspaceId,
                                     String srcPath,
                                     String srcAddress,
                                     int srcPort,
                                     boolean removeSourceOnSuccess,
                                     String srcUserName,
                                     String destPath) throws ServerException {
        CommandLine commandLine = new CommandLine(backupScript,
                                                  srcPath,
                                                  srcAddress,
                                                  Integer.toString(srcPort),
                                                  destPath,
                                                  Boolean.toString(removeSourceOnSuccess),
                                                  srcUserName);

        try {
            executeCommand(commandLine.asArray(), maxBackupDuration, srcAddress, workspaceId);
        } catch (TimeoutException e) {
            throw new ServerException("Backup of workspace " + workspaceId + " filesystem terminated due to timeout on "
                                      + srcAddress + " node.");
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(
                    "Backup of workspace " + workspaceId + " filesystem interrupted on " + srcAddress + " node.");
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(
                    "Backup of workspace " + workspaceId + " filesystem terminated on " + srcAddress + " node. "
                    + e.getLocalizedMessage());
        }
    }

    /**
     * Finds user name inside of container.
     *
     * @param workspaceId
     *         ID of workspace
     * @param containerId
     *         ID of container
     * @return {@code User} object with only username filled
     * @throws IOException
     *         if connection to container fails
     * @throws ServerException
     *         if other error occurs
     */
    private User getUserName(String workspaceId,
                             String containerId) throws IOException,
                                                        ServerException {

        ArrayList<String> output = executeCommandInContainer(workspaceId,
                                                             containerId,
                                                             "id -u -n");

        if (output.size() != 1) {
            LOG.error("{} {}. Docker output: {}", ERROR_MESSAGE_PREFIX, workspaceId, output);
            throw new ServerException(ERROR_MESSAGE_PREFIX + workspaceId);
        }
        return new User(null, null, output.get(0));
    }

    /**
     * Finds user id, group id and username inside of container.
     *
     * @param workspaceId
     *         ID of workspace
     * @param containerId
     *         ID of container
     * @return {@code User} object with id, groupId and username filled
     * @throws IOException
     *         if connection to container fails
     * @throws ServerException
     *         if other error occurs
     */
    private User getUserNameAndIds(String workspaceId,
                                   String containerId) throws IOException,
                                                              ServerException {

        ArrayList<String> output = executeCommandInContainer(workspaceId,
                                                             containerId,
                                                             "id -u && id -g && id -u -n");

        if (output.size() != 3) {
            LOG.error("{} {}. Docker output: {}", ERROR_MESSAGE_PREFIX, workspaceId, output);
            throw new ServerException(ERROR_MESSAGE_PREFIX + workspaceId);
        }
        return new User(output.get(0), output.get(1), output.get(2));
    }

    /**
     * Executes provides command inside of specified docker container and returns output.
     *
     * @param workspaceId
     *         ID of workspace
     * @param containerId
     *         ID of container
     * @param command
     *         command to execute
     * @return ArrayList with output lines as entries
     * @throws IOException
     *         if connection to container fails
     * @throws ServerException
     *         if other error occurs
     */
    private ArrayList<String> executeCommandInContainer(String workspaceId,
                                                        String containerId,
                                                        String command) throws IOException,
                                                                               ServerException {
        Exec exec = dockerConnector.createExec(CreateExecParams.create(containerId, new String[] {"sh", "-c", command})
                                                               .withDetach(false));
        ArrayList<String> execOutput = new ArrayList<>(3);
        ValueHolder<Boolean> hasFailed = new ValueHolder<>(false);
        dockerConnector.startExec(StartExecParams.create(exec.getId()), logMessage -> {
            if (logMessage.getType() != LogMessage.Type.STDOUT) {
                hasFailed.set(true);
            }
            execOutput.add(logMessage.getContent());
        });

        if (hasFailed.get()) {
            LOG.error("{} {}. Docker output: {}", ERROR_MESSAGE_PREFIX, workspaceId, execOutput);
            throw new ServerException(ERROR_MESSAGE_PREFIX + workspaceId);
        }

        return execOutput;
    }

    /**
     * Retrieves published port for SSH in machine.
     *
     * @throws ServerException
     *         if port is not found
     */
    private int getSyncPort(String containerId) throws ServerException {
        ContainerInfo containerInfo;
        try {
            containerInfo = dockerConnector.inspectContainer(containerId);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Workspace projects files in ws-machine are not accessible");
        }

        Map<String, List<PortBinding>> ports = firstNonNull(containerInfo.getNetworkSettings().getPorts(), emptyMap());
        List<PortBinding> portBindings = ports.get("22/tcp");
        if (portBindings == null || portBindings.isEmpty()) {
            // should not happen
            throw new ServerException(
                    "Sync port is not exposed in ws-machine. Workspace projects syncing is not possible");
        }

        return Integer.parseUnsignedInt(portBindings.get(0).getHostPort());
    }

    /**
     * Retrieves published port for SSH in machine.
     *
     * @throws ServerException
     *         if port is not found
     */
    private int getSyncPort(Machine machine) throws ServerException {
        Server server = machine.getRuntime().getServers().get("22/tcp");
        if (server == null) {
            throw new ServerException(
                    "Sync port is not exposed in ws-machine. Workspace projects syncing is not possible");
        }
        return Integer.parseUnsignedInt(server.getAddress().split(":", 2)[1]);
    }

    @VisibleForTesting
    void executeCommand(String[] commandLine,
                        int timeout,
                        String address,
                        String workspaceId) throws TimeoutException,
                                                   IOException,
                                                   InterruptedException {
        final ListLineConsumer outputConsumer = new ListLineConsumer();
        Process process = ProcessUtil.executeAndWait(commandLine, timeout, SECONDS, outputConsumer);

        if (process.exitValue() != 0) {
            LOG.error("Error occurred during backup/restore of workspace '{}' on node '{}' : {}",
                      workspaceId, address, outputConsumer.getText());
            throw new IOException("Synchronization process failed. Exit code " + process.exitValue());
        }
    }

    private static class User {
        String id;
        String groupId;
        String name;

        public User(String id, String groupId, String name) {
            this.id = id;
            this.groupId = groupId;
            this.name = name;
        }

    }
}
