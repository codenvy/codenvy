/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.machine;

import static org.slf4j.LoggerFactory.getLogger;

import com.codenvy.machine.backup.DockerEnvironmentBackupManager;
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.google.inject.assistedinject.Assisted;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.slf4j.Logger;

/**
 * REST client for remote machine node
 *
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
public class RemoteDockerNode implements DockerNode {
  private static final Logger LOG = getLogger(RemoteDockerNode.class);
  private static final Pattern NODE_ADDRESS =
      Pattern.compile(
          "((?<protocol>[a-zA-Z])://)?"
              +
              // http://stackoverflow.com/questions/106179/regular-expression-to-match-dns-hostname-or-ip-address
              "(?<host>(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]))"
              + ":(?<port>\\d+)");

  private final String workspaceId;
  private final DockerEnvironmentBackupManager backupManager;
  private final String containerId;
  private final String nodeHost;
  private final String nodeIp;

  private volatile boolean isBound;

  @Inject
  public RemoteDockerNode(
      DockerConnector dockerConnector,
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
            throw new MachineException(
                "Can't extract docker node address from: " + info.getNode().getAddr());
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
  public void bindWorkspace() throws ServerException, EnvironmentException {
    backupManager.restoreWorkspaceBackup(workspaceId, containerId, nodeHost);
    isBound = true;
  }

  @Override
  public void unbindWorkspace() throws ServerException {
    if (!isBound) {
      LOG.warn(
          "The container '{}' in the workspace '{}' won't be backed up. "
              + "The reason is that the workspace wasn't restored from backup",
          containerId,
          workspaceId);
    } else {
      try {
        backupManager.backupWorkspaceAndCleanup(workspaceId, containerId, nodeHost);
      } catch (EnvironmentException e) {
        LOG.info(
            "Workspace unbinding failed due to environment error. Error: "
                + e.getLocalizedMessage());
      } catch (ServerException e) {
        // TODO do throw it further when it won't brake ws stop
        LOG.error(e.getLocalizedMessage(), e);
      }
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
