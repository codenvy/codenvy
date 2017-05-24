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

import org.eclipse.che.api.environment.server.AgentConfigApplier;
import org.eclipse.che.api.environment.server.DefaultInfrastructureProvisioner;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.inject.CheBootstrap;
import org.eclipse.che.plugin.docker.machine.ext.provider.DockerExtConfBindingProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.eclipse.che.api.workspace.shared.Utils.getDevMachineName;

/**
 * Infrastructure provisioner that adds workspace machines configuration needed for running Codenvy in container.
 *
 * @author Alexander Garagatyi
 */
public class CodenvyInContainerInfrastructureProvisioner extends DefaultInfrastructureProvisioner {
    public static final String SYNC_KEY_ENV_VAR_NAME = "CODENVY_SYNC_PUB_KEY";

    private static final List<String> SNAPSHOT_EXCLUDED_DIRECTORIES = Arrays.asList("/tmp");

    private final String pubSyncKey;
    private final String projectFolder;

    @Inject
    public CodenvyInContainerInfrastructureProvisioner(AgentConfigApplier agentConfigApplier,
                                                       @Named("workspace.backup.public_key") String pubSyncKey,
                                                       @Named("che.workspace.projects.storage") String projectFolder) {
        super(agentConfigApplier);

        this.pubSyncKey = pubSyncKey;
        this.projectFolder = projectFolder;
    }

    @Override
    public void provision(EnvironmentImpl envConfig, CheServicesEnvironmentImpl internalEnv) throws EnvironmentException {
        String devMachineName = getDevMachineName(envConfig);
        if (devMachineName == null) {
            throw new EnvironmentException("ws-machine is not found on agents applying");
        }
        CheServiceImpl devMachine = internalEnv.getServices().get(devMachineName);

        // dev-machine-only configuration
        HashMap<String, String> environmentVars = new HashMap<>(devMachine.getEnvironment());
        environmentVars.put(SYNC_KEY_ENV_VAR_NAME, pubSyncKey);
        // TODO is not used, but ws-agent doesn't start without it
        environmentVars.put(CheBootstrap.CHE_LOCAL_CONF_DIR,
                                        DockerExtConfBindingProvider.EXT_CHE_LOCAL_CONF_DIR);
        devMachine.setEnvironment(environmentVars);

        ExtendedMachineImpl extendedMachine = envConfig.getMachines().get(devMachineName);
        List<String> agents = new ArrayList<>(extendedMachine.getAgents());
        agents.add(agents.indexOf("org.eclipse.che.ws-agent"), "com.codenvy.rsync_in_machine");
        extendedMachine.setAgents(agents);

        List<String> volumes = new ArrayList<>(devMachine.getVolumes());
        volumes.add(projectFolder);
        devMachine.setVolumes(volumes);

        for (CheServiceImpl service : internalEnv.getServices().values()) {
            volumes = new ArrayList<>(service.getVolumes());
            volumes.addAll(SNAPSHOT_EXCLUDED_DIRECTORIES); // creates volume for each directory to exclude from a snapshot
            service.setVolumes(volumes);
        }

        super.provision(envConfig, internalEnv);
    }

    @Override
    public void provision(ExtendedMachineImpl machineConfig, CheServiceImpl internalMachine) throws EnvironmentException {
        super.provision(machineConfig, internalMachine);
    }
}
