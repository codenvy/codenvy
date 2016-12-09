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
package com.codenvy.machine.agent.launcher;

import org.eclipse.che.api.agent.server.launcher.AbstractAgentLauncher;
import org.eclipse.che.api.agent.server.launcher.AgentLaunchingChecker;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.plugin.docker.machine.DockerInstance;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Starts ws agent in the machine and waits until ws agent sends notification about its start.
 *
 * @author Alexander Garagatyi
 */
@Singleton // TODO internal should not extend external
public class ExternalRsyncAgentLauncherImpl extends AbstractAgentLauncher {
    private static final Logger LOG = getLogger(ExternalRsyncAgentLauncherImpl.class);

    @Inject
    public ExternalRsyncAgentLauncherImpl(@Named("che.agent.dev.max_start_time_ms") long agentMaxStartTimeMs,
                                          @Named("che.agent.dev.ping_delay_ms") long agentPingDelayMs) {
        super(agentMaxStartTimeMs, agentPingDelayMs, AgentLaunchingChecker.DEFAULT);
    }

    @Override
    public String getAgentId() {
        return "com.codenvy.external_rsync";
    }

    @Override
    public String getMachineType() {
        return "docker";
    }

    @Override
    public void launch(Instance machine, Agent agent) throws ServerException {
        super.launch(machine, agent);

        DockerNode node = (DockerNode)machine.getNode();
        DockerInstance dockerMachine = (DockerInstance)machine;
        node.bindWorkspace();
        LOG.info("Docker machine has been deployed. " +
                 "ID '{}'. Workspace ID '{}'. " +
                 "Container ID '{}'. Node host '{}'. Node IP '{}'",
                 machine.getId(), machine.getWorkspaceId(),
                 dockerMachine.getContainer(), node.getHost(), node.getIp());
    }
}
