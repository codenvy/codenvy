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
package com.codenvy.machine;

import com.codenvy.machine.authentication.server.MachineTokenRegistry;
import com.google.common.base.MoreObjects;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.api.machine.server.util.RecipeRetriever;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator;
import org.eclipse.che.plugin.docker.machine.DockerInstanceProvider;
import org.eclipse.che.plugin.docker.machine.DockerInstanceStopDetector;
import org.eclipse.che.plugin.docker.machine.DockerMachineFactory;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Set;

/**
 * Docker implementation of {@link InstanceProvider}.
 * This implementation provides docker machines with environment context
 * that contains the specific machine token instead of user token.
 *
 * @author Anton Korneta
 */
public class AuthDockerInstanceProvider extends DockerInstanceProvider {
    private final MachineTokenRegistry tokenRegistry;

    @Inject
    public AuthDockerInstanceProvider(DockerConnector docker,
                                      DockerConnectorConfiguration dockerConnectorConfiguration,
                                      DockerMachineFactory dockerMachineFactory,
                                      DockerInstanceStopDetector dockerInstanceStopDetector,
                                      DockerContainerNameGenerator containerNameGenerator,
                                      RecipeRetriever recipeRetriever,
                                      @Named("machine.docker.dev_machine.machine_servers") Set<ServerConf> devMachineServers,
                                      @Named("machine.docker.machine_servers") Set<ServerConf> allMachinesServers,
                                      @Named("machine.docker.dev_machine.machine_volumes") Set<String> devMachineSystemVolumes,
                                      @Named("machine.docker.machine_volumes") Set<String> allMachinesSystemVolumes,
                                      @Nullable @Named("machine.docker.machine_extra_hosts") String allMachinesExtraHosts,
                                      WorkspaceFolderPathProvider workspaceFolderPathProvider,
                                      @Named("che.machine.projects.internal.storage") String projectFolderPath,
                                      @Named("machine.docker.pull_image") boolean doForcePullOnBuild,
                                      @Named("machine.docker.privilege_mode") boolean privilegeMode,
                                      @Named("machine.docker.dev_machine.machine_env") Set<String> devMachineEnvVariables,
                                      @Named("machine.docker.machine_env") Set<String> allMachinesEnvVariables,
                                      @Named("machine.docker.snapshot_use_registry") boolean snapshotUseRegistry,
                                      MachineTokenRegistry tokenRegistry) throws IOException {
        super(docker,
              dockerConnectorConfiguration,
              dockerMachineFactory,
              dockerInstanceStopDetector,
              containerNameGenerator,
              recipeRetriever,
              devMachineServers,
              allMachinesServers,
              devMachineSystemVolumes,
              allMachinesSystemVolumes,
              allMachinesExtraHosts,
              workspaceFolderPathProvider,
              projectFolderPath,
              doForcePullOnBuild,
              privilegeMode,
              devMachineEnvVariables,
              allMachinesEnvVariables,
              snapshotUseRegistry);
        this.tokenRegistry = tokenRegistry;
    }


    @Override
    protected String getUserToken(String wsId) {
        String userToken = null;
        try {
            userToken = tokenRegistry.getOrCreateToken(EnvironmentContext.getCurrent().getSubject().getUserId(), wsId);
        } catch (NotFoundException ignore) {
        }
        return MoreObjects.firstNonNull(userToken, "");
    }
}
