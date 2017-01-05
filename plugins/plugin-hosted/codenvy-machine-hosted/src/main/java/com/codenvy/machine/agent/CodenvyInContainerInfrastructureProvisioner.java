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
package com.codenvy.machine.agent;

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.environment.server.AgentConfigApplier;
import org.eclipse.che.api.environment.server.DefaultInfrastructureProvisioner;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.inject.CheBootstrap;
import org.eclipse.che.plugin.docker.machine.ext.provider.DockerExtConfBindingProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * Infrastructure provisioner that adds workspace machines configuration needed for running Codenvy in container.
 *
 * @author Alexander Garagatyi
 */
public class CodenvyInContainerInfrastructureProvisioner extends DefaultInfrastructureProvisioner {
    private final String pubSyncKey;

    @Inject
    public CodenvyInContainerInfrastructureProvisioner(AgentConfigApplier agentConfigApplier,
                                                       @Named("workspace.backup.public_key") String pubSyncKey) {
        super(agentConfigApplier);

        this.pubSyncKey = pubSyncKey;
    }

    @Override
    public void provision(Environment envConfig, CheServicesEnvironmentImpl internalEnv) throws EnvironmentException {
        String devMachineName = getDevMachineName(envConfig);
        CheServiceImpl devMachine = internalEnv.getServices().get(devMachineName);

        // dev-machine-only configuration
        devMachine.getEnvironment().put("CODENVY_SYNC_PUB_KEY", pubSyncKey);
        // TODO is not used, but ws-agent doesn't start without it
        devMachine.getEnvironment().put(CheBootstrap.CHE_LOCAL_CONF_DIR,
                                        DockerExtConfBindingProvider.EXT_CHE_LOCAL_CONF_DIR);

        List<String> agents = envConfig.getMachines().get(devMachineName).getAgents();
        agents.add(agents.indexOf("org.eclipse.che.ws-agent"), "com.codenvy.rsync_in_machine");

        super.provision(envConfig, internalEnv);
    }

    @Override
    public void provision(ExtendedMachine machineConfig, CheServiceImpl internalMachine) throws EnvironmentException {
        super.provision(machineConfig, internalMachine);
    }

    private String getDevMachineName(Environment envConfig) throws EnvironmentException {
        for (Map.Entry<String, ? extends ExtendedMachine> entry : envConfig.getMachines().entrySet()) {
            List<String> agents = entry.getValue().getAgents();
            if (agents != null && agents.contains("org.eclipse.che.ws-agent")) {
                return entry.getKey();
            }
        }
        throw new EnvironmentException("ws-machine is not found on agents applying");
    }
}
