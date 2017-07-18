/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.machine.agent.launcher;

import org.eclipse.che.api.agent.server.exception.AgentStartException;
import org.eclipse.che.api.agent.server.launcher.AbstractAgentLauncher;
import org.eclipse.che.api.agent.server.launcher.AgentLaunchingChecker;
import org.eclipse.che.api.agent.server.launcher.CommandExistsAgentChecker;
import org.eclipse.che.api.agent.server.launcher.CompositeAgentLaunchingChecker;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.plugin.docker.machine.DockerInstance;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Installs rsync in a machine and restores workspace projects into machine.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class MachineInnerRsyncAgentLauncherImpl extends AbstractAgentLauncher {
    private static final Logger LOG = getLogger(MachineInnerRsyncAgentLauncherImpl.class);

    @Inject
    public MachineInnerRsyncAgentLauncherImpl(@Named("che.agent.dev.max_start_time_ms") long agentMaxStartTimeMs,
                                              @Named("che.agent.dev.ping_delay_ms") long agentPingDelayMs) {
        super(agentMaxStartTimeMs,
              agentPingDelayMs,
              new CompositeAgentLaunchingChecker(new CommandExistsAgentChecker("rsync"),
                                                 AgentLaunchingChecker.DEFAULT));
    }

    @Override
    public String getAgentId() {
        return "com.codenvy.rsync_in_machine";
    }

    @Override
    public String getMachineType() {
        return "docker";
    }

    @Override
    public void launch(Instance machine, Agent agent) throws ServerException, AgentStartException {
        super.launch(machine, agent);

        DockerNode node = (DockerNode)machine.getNode();
        DockerInstance dockerMachine = (DockerInstance)machine;
        try {
            node.bindWorkspace();
        } catch (EnvironmentException e) {
            throw new AgentStartException(
                    format("Agent '%s' start failed because of an error with underlying environment. Error: %s",
                           agent.getId(), e.getLocalizedMessage()));
        }
        LOG.info("Docker machine has been deployed. " +
                 "ID '{}'. Workspace ID '{}'. " +
                 "Container ID '{}'. Node host '{}'. Node IP '{}'",
                 machine.getId(), machine.getWorkspaceId(),
                 dockerMachine.getContainer(), node.getHost(), node.getIp());
    }
}
