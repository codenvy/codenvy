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
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.environment.server.AgentConfigApplier;
import org.eclipse.che.api.environment.server.DefaultInfrastructureProvisioner;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.inject.CheBootstrap;
import org.eclipse.che.plugin.docker.machine.ext.provider.DockerExtConfBindingProvider;
import org.eclipse.che.plugin.docker.machine.ext.provider.TerminalVolumeProvider;
import org.eclipse.che.plugin.docker.machine.ext.provider.WsAgentVolumeProvider;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Infrastructure provisioner that adds workspace machines configuration needed for running Codenvy natively.
 *
 * @author Alexander Garagatyi
 */
public class NativeCodenvyInfrastructureProvisioner extends DefaultInfrastructureProvisioner {
    private final WorkspaceFolderPathProvider workspaceFolderPathProvider;
    private final WindowsPathEscaper          pathEscaper;
    private final String                      projectFolderPath;
    private final WsAgentVolumeProvider       wsAgentVolumeProvider;
    private final TerminalVolumeProvider      terminalVolumeProvider;

    @Inject
    public NativeCodenvyInfrastructureProvisioner(AgentConfigApplier agentConfigApplier,
                                                  WorkspaceFolderPathProvider workspaceFolderPathProvider,
                                                  WindowsPathEscaper pathEscaper,
                                                  @Named("che.workspace.projects.storage") String projectFolderPath,
                                                  WsAgentVolumeProvider wsAgentVolumeProvider,
                                                  TerminalVolumeProvider terminalVolumeProvider) {
        super(agentConfigApplier);

        this.workspaceFolderPathProvider = workspaceFolderPathProvider;
        this.pathEscaper = pathEscaper;
        this.projectFolderPath = projectFolderPath;
        this.wsAgentVolumeProvider = wsAgentVolumeProvider;
        this.terminalVolumeProvider = terminalVolumeProvider;

    }

    @Override
    public void provision(Environment envConfig, CheServicesEnvironmentImpl internalEnv) throws EnvironmentException {
        String devMachineName = getDevMachineName(envConfig);

        // dev-machine-only configuration
        // find path for mounting workspace FS on host
        String projectFolderVolume;
        try {
            projectFolderVolume = format("%s:%s:Z",
                                         workspaceFolderPathProvider.getPath(internalEnv.getWorkspaceId()),
                                         projectFolderPath);
        } catch (IOException e) {
            throw new EnvironmentException("Error occurred on resolving path to files of workspace " +
                                           internalEnv.getWorkspaceId());
        }
        CheServiceImpl devMachine = internalEnv.getServices().get(devMachineName);
        List<String> devMachineVolumes = devMachine.getVolumes();
        devMachineVolumes.add(SystemInfo.isWindows() ? pathEscaper.escapePath(projectFolderVolume)
                                                     : projectFolderVolume);
        devMachineVolumes.add(wsAgentVolumeProvider.get());
        // TODO is not used, but ws-agent doesn't start without it
        devMachine.getEnvironment().put(CheBootstrap.CHE_LOCAL_CONF_DIR,
                                        DockerExtConfBindingProvider.EXT_CHE_LOCAL_CONF_DIR);

        List<String> agents = envConfig.getMachines().get(devMachineName).getAgents();
        agents.add(agents.indexOf("org.eclipse.che.ws-agent"), "com.codenvy.external_rsync");

        // common machine configuration
        for (CheServiceImpl machine : internalEnv.getServices().values()) {
            machine.getVolumes().add(terminalVolumeProvider.get());
        }

        super.provision(envConfig, internalEnv);
    }

    @Override
    public void provision(ExtendedMachine machineConfig, CheServiceImpl internalMachine) throws EnvironmentException {
        internalMachine.getVolumes().add(terminalVolumeProvider.get());

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
