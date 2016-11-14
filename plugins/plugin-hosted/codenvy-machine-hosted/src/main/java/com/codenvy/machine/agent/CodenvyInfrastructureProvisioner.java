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
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.environment.server.AgentConfigApplier;
import org.eclipse.che.api.environment.server.DefaultInfrastructureProvisioner;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Infrastructure provisioner that adds volume/agent for workspace files synchronization.
 * Different strategies of synchronization are switched by property {@link #SYNC_STRATEGY_PROPERTY}.
 *
 * @author Alexander Garagatyi
 */
public class CodenvyInfrastructureProvisioner extends DefaultInfrastructureProvisioner {
    public static final String SYNC_STRATEGY_PROPERTY = "codenvy.sync.strategy";

    private final String                      pubSyncKey;
    private final WorkspaceFolderPathProvider workspaceFolderPathProvider;
    private final WindowsPathEscaper          pathEscaper;
    private final String                      projectFolderPath;
    private final boolean                     syncAgentInMachine;

    @Inject
    public CodenvyInfrastructureProvisioner(AgentConfigApplier agentConfigApplier,
                                            @Named("workspace.backup.public_key") String pubSyncKey,
                                            @Named(SYNC_STRATEGY_PROPERTY) String syncStrategy,
                                            WorkspaceFolderPathProvider workspaceFolderPathProvider,
                                            WindowsPathEscaper pathEscaper,
                                            @Named("che.workspace.projects.storage") String projectFolderPath) {
        super(agentConfigApplier);
        this.pubSyncKey = pubSyncKey;
        this.workspaceFolderPathProvider = workspaceFolderPathProvider;
        this.pathEscaper = pathEscaper;
        this.projectFolderPath = projectFolderPath;
        switch (syncStrategy) {
            case "rsync":
                syncAgentInMachine = false;
                break;
            case "rsync-agent":
                syncAgentInMachine = true;
                break;
            default:
                throw new RuntimeException(
                        format("Property '%s' has illegal value '%s'. Valid values: rsync, rsync-agent",
                               SYNC_STRATEGY_PROPERTY,
                               syncStrategy));
        }
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
        String syncAgentId;
        if (syncAgentInMachine) {
            syncAgentId = "com.codenvy.rsync_in_machine";
            internalEnv.getServices()
                       .get(devMachineName)
                       .getEnvironment()
                       .put("CODENVY_SYNC_PUB_KEY", pubSyncKey);
        } else {
            syncAgentId = "com.codenvy.external_rsync";
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
            internalEnv.getServices()
                       .get(devMachineName)
                       .getVolumes()
                       .add(SystemInfo.isWindows() ? pathEscaper.escapePath(projectFolderVolume)
                                                   : projectFolderVolume);
        }
        List<String> agents = envConfig.getMachines().get(devMachineName).getAgents();
        agents.add(agents.indexOf("org.eclipse.che.ws-agent"), syncAgentId);

        super.provision(envConfig, internalEnv);
    }
}
