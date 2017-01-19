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
package com.codenvy.machine;

import com.codenvy.machine.authentication.server.MachineTokenRegistry;
import com.google.common.base.MoreObjects;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SourceNotFoundException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.machine.DockerInstanceStopDetector;
import org.eclipse.che.plugin.docker.machine.DockerMachineFactory;
import org.eclipse.che.plugin.docker.machine.MachineProviderImpl;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_KEY;
import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_VALUE;
import static java.lang.Thread.sleep;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Specific implementation of {@link MachineProviderImpl} needed for hosted environment.
 *
 * <p/> This implementation:
 * <br/>- provides compose services with environment context that contains the specific machine token instead of user token
 * <br/>- workarounds buggy pulling on swarm by replacing it with build
 * <br/>- add constraints to build to avoid docker image building on a node under maintenance
 *
 * @author Anton Korneta
 * @author Roman Iuvshyn
 * @author Alexander Garagatyi
 * @author Mykola Morhun
 */
public class HostedMachineProviderImpl extends MachineProviderImpl {
    private static final Logger LOG = getLogger(HostedMachineProviderImpl.class);

    private final DockerConnector                               docker;
    private final UserSpecificDockerRegistryCredentialsProvider dockerCredentials;
    private final MachineTokenRegistry                          tokenRegistry;

    private final ExecutorService snapshotImagesCleanerService;

    @Inject
    public HostedMachineProviderImpl(DockerConnector docker,
                                     UserSpecificDockerRegistryCredentialsProvider dockerCredentials,
                                     DockerMachineFactory dockerMachineFactory,
                                     DockerInstanceStopDetector dockerInstanceStopDetector,
                                     WindowsPathEscaper windowsPathEscaper,
                                     @Named("machine.docker.dev_machine.machine_servers") Set<ServerConf> devMachineServers,
                                     @Named("machine.docker.machine_servers") Set<ServerConf> allMachinesServers,
                                     @Named("machine.docker.dev_machine.machine_volumes") Set<String> devMachineSystemVolumes,
                                     @Named("machine.docker.machine_volumes") Set<String> allMachinesSystemVolumes,
                                     @Named("che.docker.always_pull_image") boolean doForcePullOnBuild,
                                     @Named("che.docker.privileged") boolean privilegedMode,
                                     @Named("che.docker.pids_limit") int pidsLimit,
                                     @Named("machine.docker.dev_machine.machine_env") Set<String> devMachineEnvVariables,
                                     @Named("machine.docker.machine_env") Set<String> allMachinesEnvVariables,
                                     @Named("che.docker.registry_for_snapshots") boolean snapshotUseRegistry,
                                     @Named("che.docker.swap") double memorySwapMultiplier,
                                     MachineTokenRegistry tokenRegistry,
                                     @Named("machine.docker.networks") Set<Set<String>> additionalNetworks,
                                     @Nullable @Named("che.docker.network_driver") String networkDriver,
                                     @Nullable @Named("che.docker.parent_cgroup") String parentCgroup,
                                     @Nullable @Named("che.docker.cpuset_cpus") String cpusetCpus,
                                     @Named("che.docker.cpu_period") long cpuPeriod,
                                     @Named("che.docker.cpu_quota") long cpuQuota,
                                     @Named("che.docker.extra_hosts") Set<Set<String>> additionalHosts)
            throws IOException {
        super(docker,
              dockerCredentials,
              dockerMachineFactory,
              dockerInstanceStopDetector,
              devMachineServers,
              allMachinesServers,
              devMachineSystemVolumes,
              allMachinesSystemVolumes,
              doForcePullOnBuild,
              privilegedMode,
              pidsLimit,
              devMachineEnvVariables,
              allMachinesEnvVariables,
              snapshotUseRegistry,
              memorySwapMultiplier,
              additionalNetworks,
              networkDriver,
              parentCgroup,
              cpusetCpus,
              cpuPeriod,
              cpuQuota,
              windowsPathEscaper,
              additionalHosts);

        this.docker = docker;
        this.dockerCredentials = dockerCredentials;
        this.tokenRegistry = tokenRegistry;

        this.snapshotImagesCleanerService = Executors.newFixedThreadPool(16);
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
    protected void pullImage(CheServiceImpl service,
                             String machineImageName,
                             ProgressMonitor progressMonitor) throws MachineException {

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

        } catch (ImageNotFoundException e) {
            throw new SourceNotFoundException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        } finally {
            if (workDir != null) {
                FileCleaner.addFile(workDir);
            }
            // We use build instead of pull because of better stability.
            // When new image is built it pulls base image before. This operation is performed by docker build command.
            // So, after build it is needed to cleanup base image if it is a snapshot.
            if (service.getImage().contains(MACHINE_SNAPSHOT_PREFIX)) {
                // Sometimes swarm cannot delete image after its pull during few seconds.
                // To workaround that problem and avoid redundant delay on workspace start
                // we must clean up snapshot image in separate thread.
                // TODO remove this executor after fix of the problem in pure Docker Swarm
                snapshotImagesCleanerService.submit(() -> {
                    try {
                        // This sleep is needed for swarm to see just pulled image. Otherwise deletion may fail.
                        sleep(10_000);
                        docker.removeImage(service.getImage());
                    } catch (IOException e) {
                        if (!e.getMessage().contains("No such image")) { // ignore error if image already deleted
                            LOG.error("Failed to delete pulled snapshot: " + service.getImage());
                        }
                    } catch (InterruptedException e) {
                        try {
                            docker.removeImage(service.getImage());
                        } catch (IOException ignore) { }
                    }
                });
            }
        }
    }

    /**
     * This method adds constraint to build args for support 'scheduled for maintenance' labels of nodes.
     *
     * {@inheritDoc}
     */
    @Override
    protected void buildImage(CheServiceImpl service,
                              String machineImageName,
                              boolean doForcePullOnBuild,
                              ProgressMonitor progressMonitor) throws MachineException {
        File workDir = null;
        try {
            BuildImageParams buildImageParams;
            if (service.getBuild() != null &&
                service.getBuild().getDockerfileContent() != null) {

                workDir = Files.createTempDirectory(null).toFile();
                final File dockerfileFile = new File(workDir, "Dockerfile");
                try (FileWriter output = new FileWriter(dockerfileFile)) {
                    output.append(service.getBuild().getDockerfileContent());
                }

                buildImageParams = BuildImageParams.create(dockerfileFile);
            } else {
                buildImageParams = BuildImageParams.create(service.getBuild().getContext())
                                                   .withDockerfile(service.getBuild().getDockerfilePath());
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
