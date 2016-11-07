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
package com.codenvy.machine.launcher;

import com.codenvy.machine.RemoteDockerNode;

import org.eclipse.che.api.agent.server.launcher.AbstractAgentLauncher;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.spi.Instance;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Starts ws agent in the machine and waits until ws agent sends notification about its start.
 *
 * @author Alexander Garagatyi
 * @author Anatolii Bazko
 */
@Singleton
public class RsyncAgentLauncherImpl extends AbstractAgentLauncher {
    @Inject
    public RsyncAgentLauncherImpl(@Named("che.agent.dev.max_start_time_ms") long agentMaxStartTimeMs,
                                  @Named("che.agent.dev.ping_delay_ms") long agentPingDelayMs) {
        super(agentMaxStartTimeMs, agentPingDelayMs, new NoOpAgentLaunchingChecker());
    }

    @Override
    public String getAgentId() {
        return "org.eclipse.che.rsync";
    }

    @Override
    public String getMachineType() {
        return "docker";
    }

    @Override
    public void launch(Instance machine, Agent agent) throws ServerException {
        // TODO move pubkey from env of container to agent
        super.launch(machine, agent);

        RemoteDockerNode node = (RemoteDockerNode)machine.getNode();
        node.bindWorkspace();
    }
}
