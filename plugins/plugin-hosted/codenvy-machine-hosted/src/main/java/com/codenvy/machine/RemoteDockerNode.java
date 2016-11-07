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

import com.codenvy.machine.backup.MachineBackupManager;
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Collections.emptyMap;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * REST client for remote machine node
 *
 * @author Alexander Garagatyi
 */
public class RemoteDockerNode implements DockerNode {
    private static final Logger  LOG                  = getLogger(RemoteDockerNode.class);
    private static final String  ERROR_MESSAGE_PREFIX = "Can't detect container user ids to chown backed up files of workspace ";
    private static final Pattern NODE_ADDRESS         = Pattern.compile(
            "((?<protocol>[a-zA-Z])://)?" +
            // http://stackoverflow.com/questions/106179/regular-expression-to-match-dns-hostname-or-ip-address
            "(?<host>(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]))" +
            ":(?<port>\\d+)");

    private final String               workspaceId;
    private final MachineBackupManager backupManager;
    private final DockerConnector      dockerConnector;
    private final String               containerId;
    private final String               hostProjectsFolder;
    private final String               nodeHost;

    @Inject
    public RemoteDockerNode(DockerConnector dockerConnector,
                            @Assisted("container") String containerId,
                            @Assisted("workspace") String workspaceId,
                            MachineBackupManager backupManager,
                            WorkspaceFolderPathProvider workspaceFolderPathProvider) throws MachineException {
        this.workspaceId = workspaceId;
        this.backupManager = backupManager;
        this.dockerConnector = dockerConnector;
        this.containerId = containerId;

        try {
            String nodeHost = "127.0.0.1";
            this.hostProjectsFolder = workspaceFolderPathProvider.getPath(workspaceId);
            if (dockerConnector instanceof SwarmDockerConnector) {

                final ContainerInfo info = dockerConnector.inspectContainer(containerId);
                if (info != null) {
                    final Matcher matcher = NODE_ADDRESS.matcher(info.getNode().getAddr());
                    if (matcher.matches()) {
                        nodeHost = matcher.group("host");
                    } else {
                        throw new MachineException("Can't extract docker node address from: " + info.getNode().getAddr());
                    }
                }
            }
            this.nodeHost = nodeHost;
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new MachineException("Internal server error occurs. Please contact support");
        }
    }

    @Override
    public void bindWorkspace() throws MachineException {
        try {
            final Exec exec = dockerConnector.createExec(CreateExecParams.create(containerId,
                                                                                 new String[] {"/bin/sh",
                                                                                               "-c",
                                                                                               "id -u && id -g"})
                                                                         .withDetach(false));
            final List<String> execOutputs = new ArrayList<>();
            final ValueHolder<Boolean> hasFailed = new ValueHolder<>(false);
            dockerConnector.startExec(StartExecParams.create(exec.getId()), logMessage -> {
                if (logMessage.getType() != LogMessage.Type.STDOUT) {
                    hasFailed.set(true);
                }
                execOutputs.add(logMessage.getContent());
            });

            if (hasFailed.get() || execOutputs.size() < 2) {
                LOG.error("{} {}. Docker output: {}", ERROR_MESSAGE_PREFIX, workspaceId, execOutputs);
                throw new MachineException(ERROR_MESSAGE_PREFIX + workspaceId);
            }

            backupManager.restoreWorkspaceBackup(workspaceId,
                                                 hostProjectsFolder,
                                                 execOutputs.get(0),
                                                 execOutputs.get(1),
                                                 nodeHost,
                                                 getSshPort());
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new MachineException("Can't restore workspace file system");
        } catch (ServerException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void unbindWorkspace() throws MachineException {
        try {
            backupManager.backupWorkspaceAndCleanup(workspaceId,
                                                    hostProjectsFolder,
                                                    nodeHost,
                                                    getSshPort());
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public String getProjectsFolder() {
        return hostProjectsFolder;
    }

    @Override
    public String getHost() {
        return nodeHost;
    }

    // TODO what to do here?
    private String getSshPort() throws MachineException {
        ContainerInfo containerInfo;
        try {
            containerInfo = dockerConnector.inspectContainer(containerId);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new MachineException("Can't unbind workspace");
        }

        Map<String, List<PortBinding>> ports = firstNonNull(containerInfo.getNetworkSettings().getPorts(), emptyMap());
        List<PortBinding> portBindings = ports.get("22/tcp");
        if (portBindings == null || portBindings.isEmpty()) {
            throw new MachineException("Can't unbind workspace");
        }

        return portBindings.get(0).getHostPort();
    }
}
