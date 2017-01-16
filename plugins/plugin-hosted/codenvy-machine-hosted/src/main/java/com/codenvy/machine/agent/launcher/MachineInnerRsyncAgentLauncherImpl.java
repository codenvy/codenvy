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
package com.codenvy.machine.agent.launcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Installs rsync in a machine and restores workspace projects into machine.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class MachineInnerRsyncAgentLauncherImpl extends ExternalRsyncAgentLauncherImpl {
    @Inject
    public MachineInnerRsyncAgentLauncherImpl(@Named("che.agent.dev.max_start_time_ms") long agentMaxStartTimeMs,
                                              @Named("che.agent.dev.ping_delay_ms") long agentPingDelayMs) {
        super(agentMaxStartTimeMs, agentPingDelayMs);
    }

    @Override
    public String getAgentId() {
        return "com.codenvy.rsync_in_machine";
    }
}
