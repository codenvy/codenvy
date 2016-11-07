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

import org.eclipse.che.api.agent.server.launcher.AgentLaunchingChecker;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;

/**
 * @author Alexander Garagatyi
 */
public class NoOpAgentLaunchingChecker implements AgentLaunchingChecker {
    @Override
    public boolean isLaunched(Agent agent, InstanceProcess process, Instance machine) throws MachineException {
        return true;
    }
}
