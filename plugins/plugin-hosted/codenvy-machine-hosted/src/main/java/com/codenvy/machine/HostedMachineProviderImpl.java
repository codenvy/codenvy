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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.core.util.JsonRpcEndpointToMachineNameHolder;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SourceNotFoundException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorProvider;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.exception.ContainerNotFoundException;
import org.eclipse.che.plugin.docker.client.exception.DockerException;
import org.eclipse.che.plugin.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.machine.DockerInstanceStopDetector;
import org.eclipse.che.plugin.docker.machine.DockerMachineFactory;
import org.eclipse.che.plugin.docker.machine.MachineProviderImpl;
import org.slf4j.Logger;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_KEY;
import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_VALUE;
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

    @VisibleForTesting
    static int SWARM_WAIT_BEFORE_REPEAT_WORKAROUND_TIME_MS = 5_000;

    private final DockerConnector                               docker;
    private final UserSpecificDockerRegistryCredentialsProvider dockerCredentials;
    private final MachineTokenRegistry                          tokenRegistry;
    private final String                                        cpusetCpus;
    private final long                                          cpuPeriod;
    private final long                                          cpuQuota;
    private final Map<String, String>                           buildArgs;

    private final ScheduledExecutorService snapshotImagesCleanerService;

    @Inject
    public HostedMachineProviderImpl(DockerConnectorProvider dockerConnectorProvider,
                                     UserSpecificDockerRegistryCredentialsProvider dockerCredentials,
                                     DockerMachineFactory dockerMachineFactory,
                                     DockerInstanceStopDetector dockerInstanceStopDetector,
                                     WindowsPathEscaper windowsPathEscaper,
                                     RequestTransmitter requestTransmitter,
                                     JsonRpcEndpointToMachineNameHolder endpointIdsHolder,
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
                                     @Named("che.docker.extra_hosts") Set<Set<String>> additionalHosts,
                                     @Nullable @Named("che.docker.dns_resolvers") String[] dnsResolvers,
                                     @Named("che.docker.build_args") Map<String, String> buildArgs)
            throws IOException {
        super(dockerConnectorProvider,
              dockerCredentials,
              dockerMachineFactory,
              dockerInstanceStopDetector,
              requestTransmitter,
              endpointIdsHolder,
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
              additionalHosts,
              dnsResolvers,
              buildArgs);

        this.docker = dockerConnectorProvider.get();
        this.dockerCredentials = dockerCredentials;
        this.tokenRegistry = tokenRegistry;
        this.cpusetCpus = cpusetCpus;
        this.cpuPeriod = cpuPeriod;
        this.cpuQuota = cpuQuota;
        this.buildArgs = new HashMap<>(buildArgs);
        // don't build an image on a node under maintenance
        this.buildArgs.put(MAINTENANCE_CONSTRAINT_KEY, MAINTENANCE_CONSTRAINT_VALUE);

        this.snapshotImagesCleanerService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("SnapshotImagesCleaner")
                                          .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                                          .setDaemon(false)
                                          .build());
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

        String image = service.getImage();
        File workDir = null;
        try {
            // build docker image
            workDir = Files.createTempDirectory(null).toFile();
            final File dockerfileFile = new File(workDir, "Dockerfile");
            try (FileWriter output = new FileWriter(dockerfileFile)) {
                output.append("FROM ").append(image);
            }

            docker.buildImage(BuildImageParams.create(dockerfileFile)
                                              .withForceRemoveIntermediateContainers(true)
                                              .withRepository(machineImageName)
                                              .withAuthConfigs(dockerCredentials.getCredentials())
                                              .withDoForcePull(true)
                                              .withMemoryLimit(service.getMemLimit())
                                              .withMemorySwapLimit(-1)
                                              .withCpusetCpus(cpusetCpus)
                                              .withCpuPeriod(cpuPeriod)
                                              .withCpuQuota(cpuQuota)
                                              .withBuildArgs(buildArgs),
                              progressMonitor);
        } catch (ImageNotFoundException e) {
            throw new SourceNotFoundException(e.getLocalizedMessage(), e);
        } catch (DockerException e) {
            // Check whether image to pull is a snapshot and if so then fallback to workspace recipe.
            if (image != null && SNAPSHOT_LOCATION_PATTERN.matcher(image).matches()) {
                throw new SourceNotFoundException(e.getLocalizedMessage(), e);
            }
            throw new MachineException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        } finally {
            if (workDir != null) {
                FileCleaner.addFile(workDir);
            }
            // When new image is being built it pulls base image. This operation is performed by docker build command.
            // So, after build it is needed to cleanup base image if it is a snapshot.
            if (service.getImage().contains(MACHINE_SNAPSHOT_PREFIX)) {
                submitCleanSnapshotImageTask(service.getImage());
            }
        }
    }

    /**
     * Sometimes swarm cannot delete image after its pull during a few seconds.
     * To workaround that problem and avoid redundant delay on workspace start
     * we must clean up snapshot image in separate thread after some delay.
     *
     * @param image
     *         image to clean, e.g. 172.11.12.13:5000/machine_snapshot_abcdef1234567890:latest
     */
    private void submitCleanSnapshotImageTask(String image) {
        // TODO replace this method by docker.removeImage(image) call after fix of the problem in pure Docker Swarm
        snapshotImagesCleanerService.schedule(() -> {
            try {
                docker.removeImage(image);
            } catch (IOException e) {
                if (!e.getMessage().contains("No such image")) { // ignore error if image already deleted
                    LOG.error("Failed to delete pulled snapshot: " + image);
                }
            }
        }, 10L, TimeUnit.SECONDS);
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
            Map<String, String> buildArgs;
            if (service.getBuild().getArgs() == null || service.getBuild().getArgs().isEmpty()) {
                buildArgs = this.buildArgs;
            } else {
                buildArgs = new HashMap<>(this.buildArgs);
                buildArgs.putAll(service.getBuild().getArgs());
            }

            buildImageParams.withForceRemoveIntermediateContainers(true)
                            .withRepository(machineImageName)
                            .withAuthConfigs(dockerCredentials.getCredentials())
                            .withDoForcePull(doForcePullOnBuild)
                            .withMemoryLimit(service.getMemLimit())
                            .withMemorySwapLimit(-1)
                            .withCpusetCpus(cpusetCpus)
                            .withCpuPeriod(cpuPeriod)
                            .withCpuQuota(cpuQuota)
                            .withBuildArgs(buildArgs);

            docker.buildImage(buildImageParams, progressMonitor);
        } catch (IOException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        } finally {
            if (workDir != null) {
                FileCleaner.addFile(workDir);
            }
        }
    }

    @Override
    protected void setNonExitingContainerCommandIfNeeded(ContainerConfig containerConfig) throws IOException {
        // Sometimes Swarm doesn't see newly created images for several seconds.
        // Here we retry operation to ensure that such behavior of Swarm doesn't affect the product.
        try {
            super.setNonExitingContainerCommandIfNeeded(containerConfig);
        } catch (ImageNotFoundException e) {
            try {
                Thread.sleep(SWARM_WAIT_BEFORE_REPEAT_WORKAROUND_TIME_MS);
                super.setNonExitingContainerCommandIfNeeded(containerConfig);
            } catch (InterruptedException ignored) {
                // throw original error
                throw e;
            }
        }
    }

    @Override
    protected void checkContainerIsRunning(String container) throws IOException, ServerException {
        // Sometimes Swarm doesn't see newly created containers for several seconds.
        // Here we retry operation to ensure that such behavior of Swarm doesn't affect the product.
        try {
            super.checkContainerIsRunning(container);
        } catch (ContainerNotFoundException e) {
            try {
                Thread.sleep(SWARM_WAIT_BEFORE_REPEAT_WORKAROUND_TIME_MS);
                super.checkContainerIsRunning(container);
            } catch (InterruptedException ignored) {
                // throw original error
                throw e;
            }
        }
    }

    @PreDestroy
    private void finalizeSnapshotImagesCleaner() {
        snapshotImagesCleanerService.shutdown();
        try {
            if (!snapshotImagesCleanerService.awaitTermination(30L, TimeUnit.SECONDS)) {
                snapshotImagesCleanerService.shutdownNow();
                if (!snapshotImagesCleanerService.awaitTermination(10L, TimeUnit.SECONDS)) {
                    LOG.warn("Failed to terminate SnapshotImagesCleaner scheduler");
                }
            }
        } catch (InterruptedException e) {
            snapshotImagesCleanerService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
