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
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.environment.server.compose.model.ComposeServiceImpl;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SourceNotFoundException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.machine.ComposeMachineProviderImpl;
import org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator;
import org.eclipse.che.plugin.docker.machine.DockerInstanceStopDetector;
import org.eclipse.che.plugin.docker.machine.DockerMachineFactory;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_KEY;
import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_VALUE;

/**
 * Specific implementation of {@link ComposeMachineProviderImpl} needed for hosted environment.
 *
 * <p/> This implementation:
 * <br/>- provides compose services with environment context that contains the specific machine token instead of user token
 * <br/>- workarounds buggy pulling on swarm by replacing it with build
 * <br/>- add constraints to build to avoid docker image building on a node under maintenance
 *
 * @author Anton Korneta
 * @author Roman Iuvshyn
 * @author Alexander Garagatyi
 */
public class HostedComposeMachineProviderImpl extends ComposeMachineProviderImpl {
    private final DockerConnector                               docker;
    private final UserSpecificDockerRegistryCredentialsProvider dockerCredentials;
    private final MachineTokenRegistry                          tokenRegistry;

    @Inject
    public HostedComposeMachineProviderImpl(DockerConnector docker,
                                            DockerConnectorConfiguration dockerConnectorConfiguration,
                                            UserSpecificDockerRegistryCredentialsProvider dockerCredentials,
                                            DockerMachineFactory dockerMachineFactory,
                                            DockerInstanceStopDetector dockerInstanceStopDetector,
                                            DockerContainerNameGenerator containerNameGenerator,
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
                                            @Named("machine.docker.memory_swap_multiplier") double memorySwapMultiplier,
                                            MachineTokenRegistry tokenRegistry,
                                            @Named("machine.docker.networks") Set<Set<String>> additionalNetworks)
            throws IOException {
        super(docker,
              dockerConnectorConfiguration,
              dockerCredentials,
              dockerMachineFactory,
              dockerInstanceStopDetector,
              containerNameGenerator,
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
              snapshotUseRegistry,
              memorySwapMultiplier,
              additionalNetworks);

        this.docker = docker;
        this.dockerCredentials = dockerCredentials;
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

    /**
     * Origin pull image is unstable with swarm.
     * This method workarounds that with performing docker build instead of docker pull.
     *
     * {@inheritDoc}
     */
    @Override
    protected void pullImage(ComposeServiceImpl service,
                             String machineImageName,
                             ProgressMonitor progressMonitor) throws NotFoundException,
                                                                     MachineException,
                                                                     SourceNotFoundException {

        File workDir = null;
        try {
            // build docker image
            workDir = Files.createTempDirectory(null).toFile();
            final File dockerfileFile = new File(workDir, "Dockerfile");
            try (FileWriter output = new FileWriter(dockerfileFile)) {
                output.append("FROM ").append(service.getImage());
            }

            docker.buildImage(BuildImageParams.create(dockerfileFile)
                                              .withForceRemoveIntermediateContainers(true)
                                              .withRepository(machineImageName)
                                              .withAuthConfigs(dockerCredentials.getCredentials())
                                              .withDoForcePull(true)
                                              .withMemoryLimit(service.getMemLimit())
                                              .withMemorySwapLimit(-1)
                                              // don't build an image on a node under maintenance
                                              .addBuildArg(MAINTENANCE_CONSTRAINT_KEY, MAINTENANCE_CONSTRAINT_VALUE),
                              progressMonitor);
        } catch (IOException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        } finally {
            if (workDir != null) {
                FileCleaner.addFile(workDir);
            }
        }
    }

    /**
     * This method adds constraint to build args for support 'scheduled for maintenance' labels of nodes.
     *
     * {@inheritDoc}
     */
    @Override
    protected void buildImage(ComposeServiceImpl service,
                              String machineImageName,
                              boolean doForcePullOnBuild,
                              ProgressMonitor progressMonitor) throws MachineException {
        File workDir = null;
        try {
            BuildImageParams buildImageParams;
            if (service.getBuild() != null &&
                service.getBuild().getContext() == null &&
                service.getBuild().getDockerfile() != null) {

                workDir = Files.createTempDirectory(null).toFile();
                final File dockerfileFile = new File(workDir, "Dockerfile");
                try (FileWriter output = new FileWriter(dockerfileFile)) {
                    output.append(service.getBuild().getDockerfile());
                }

                buildImageParams = BuildImageParams.create(dockerfileFile);
            } else {
                buildImageParams = BuildImageParams.create(service.getBuild().getContext())
                                                   .withDockerfile(service.getBuild().getDockerfile());
            }
            buildImageParams.withForceRemoveIntermediateContainers(true)
                            .withRepository(machineImageName)
                            .withAuthConfigs(dockerCredentials.getCredentials())
                            .withDoForcePull(doForcePullOnBuild)
                            .withMemoryLimit(service.getMemLimit())
                            .withMemorySwapLimit(-1)
                            // don't build an image on a node under maintenance
                            .addBuildArg(MAINTENANCE_CONSTRAINT_KEY, MAINTENANCE_CONSTRAINT_VALUE);

            docker.buildImage(buildImageParams, progressMonitor);
        } catch (IOException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        } finally {
            if (workDir != null) {
                FileCleaner.addFile(workDir);
            }
        }
    }
}
