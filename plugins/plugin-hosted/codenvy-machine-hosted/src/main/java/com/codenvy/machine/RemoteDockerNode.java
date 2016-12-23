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
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * REST client for remote machine node
 *
 * @author Alexander Garagatyi
 */
public class RemoteDockerNode implements DockerNode {
    private static final Logger  LOG                  = getLogger(RemoteDockerNode.class);
    private static final Pattern NODE_ADDRESS         = Pattern.compile(
            "((?<protocol>[a-zA-Z])://)?" +
            // http://stackoverflow.com/questions/106179/regular-expression-to-match-dns-hostname-or-ip-address
            "(?<host>(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]))" +
            ":(?<port>\\d+)");

    private final String                         workspaceId;
    private final DockerEnvironmentBackupManager backupManager;
    private final String                         containerId;
    private final String                         nodeHost;
    private final String                         nodeIp;

    @Inject
    public RemoteDockerNode(DockerConnector dockerConnector,
                            @Assisted("container") String containerId,
                            @Assisted("workspace") String workspaceId,
                            DockerEnvironmentBackupManager backupManager)
            throws MachineException {

        this.workspaceId = workspaceId;
        this.backupManager = backupManager;
        this.containerId = containerId;

        try {
            String nodeHost = "127.0.0.1";
            String nodeIp = "127.0.0.1";
            if (dockerConnector instanceof SwarmDockerConnector) {

                final ContainerInfo info = dockerConnector.inspectContainer(containerId);
                if (info != null) {
                    final Matcher matcher = NODE_ADDRESS.matcher(info.getNode().getAddr());
                    if (matcher.matches()) {
                        nodeHost = matcher.group("host");
                    } else {
                        throw new MachineException("Can't extract docker node address from: " + info.getNode().getAddr());
                    }
                    nodeIp = info.getNode().getIP();
                }
            }
            this.nodeHost = nodeHost;
            this.nodeIp = nodeIp;
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new MachineException("Internal server error occurs. Please contact support");
        }
    }

    @Override
    public void bindWorkspace() throws ServerException {
        backupManager.restoreWorkspaceBackup(workspaceId,
                                             containerId,
                                             nodeHost);
    }

    @Override
    public void unbindWorkspace() throws ServerException {
        try {
            backupManager.backupWorkspaceAndCleanup(workspaceId,
                                                    containerId,
                                                    nodeHost);
        } catch (ServerException e) {
            // TODO do throw it further when it won't brake ws stop
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public String getHost() {
        return nodeHost;
    }

    @Override
    public String getIp() {
        return nodeIp;
    }
}
