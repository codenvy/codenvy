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
package com.codenvy.machine.agent;

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.environment.server.AgentConfigApplier;
import org.eclipse.che.api.environment.server.DefaultInfrastructureProvisioner;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;

import javax.inject.Named;
import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
public class CodenvyInfrastructureProvisioner extends DefaultInfrastructureProvisioner {
    private final String pubSyncKey;

    public CodenvyInfrastructureProvisioner(AgentConfigApplier agentConfigApplier,
                                            @Named("workspace.backup.public_key") String pubSyncKey) {
        super(agentConfigApplier);
        this.pubSyncKey = pubSyncKey;
    }

    @Override
    public void provision(Environment envConfig, CheServicesEnvironmentImpl internalEnv) throws EnvironmentException {
        String devMachineName = envConfig.getMachines()
                                         .entrySet()
                                         .stream()
                                         .filter(entry -> entry.getValue()
                                                               .getAgents() != null &&
                                                          entry.getValue()
                                                               .getAgents()
                                                               .contains("org.eclipse.che.ws-agent"))
                                         .map(Map.Entry::getKey)
                                         .findAny()
                                         .orElseThrow(() -> new EnvironmentException(
                                                 "ws-machine is not found on agents applying"));

        envConfig.getMachines()
                 .get(devMachineName)
                 .getAgents()
                 .add("org.eclipse.che.rsync-synchronizer");
        internalEnv.getServices()
                   .get(devMachineName)
                   .getEnvironment()
                   .put("CODENVY_SYNC_PUB_KEY", pubSyncKey);

        super.provision(envConfig, internalEnv);
    }
}
