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
package com.codenvy.swarm.client;

import com.codenvy.swarm.client.model.DockerNode;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.DockerApiVersionPathPrefixProvider;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.MessageProcessor;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.exception.DockerException;
import org.eclipse.che.plugin.docker.client.exception.ExecNotFoundException;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ExecInfo;
import org.eclipse.che.plugin.docker.client.json.SystemInfo;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.primitives.Ints.tryParse;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Swarm implementation of {@link DockerConnector} that can be used on distributed system
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class SwarmDockerConnector extends DockerConnector {
    private static final Logger LOG = getLogger(SwarmDockerConnector.class);
    private static final Pattern IMAGE_NOT_FOUND_BY_SWARM_ERROR_MESSAGE = Pattern.compile("^Error: image .* not found.*", Pattern.DOTALL);
    private static final Pattern REPOSITORY_NOT_FOUND_BY_SWARM_ERROR_MESSAGE =
            Pattern.compile(".*repository .* not found: does not exist or no pull access.*", Pattern.DOTALL);

    private final NodeSelectionStrategy   strategy;
    //TODO should it be done in other way?
    private final String                  nodeDaemonScheme;
    private final int                     nodeDescriptionLength;
    // Map of exec ID to container ID (or name)
    // Temporary solution to investigate why swarm returns 404 on exec start
    private final Cache<String, String>   execToContainer;

    @Inject
    public SwarmDockerConnector(DockerConnectorConfiguration connectorConfiguration,
                                DockerConnectionFactory connectionFactory,
                                DockerRegistryAuthResolver authManager,
                                @Named("swarm.client.node_description_length") int nodeDescriptionLength,
                                DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider) {
        super(connectorConfiguration, connectionFactory, authManager, dockerApiVersionPathPrefixProvider);
        this.nodeDescriptionLength = nodeDescriptionLength;
        this.strategy = new RandomNodeSelectionStrategy();
        this.nodeDaemonScheme = "http";
        // entry is not needed after start of exec, and expiration doesn't change anything important
        // start should go right after, so expire entry after 1 minute timeout
        this.execToContainer = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Very unstable behavior with multiple nodes, try to workaround somehow
     */
    @Deprecated
    @Override
    public void pull(PullParams params, ProgressMonitor progressMonitor) throws IOException {
        final DockerNode node = strategy.select(getAvailableNodes());
        super.pull(params, progressMonitor, addrToUri(node.getAddr()));
    }

    @Override
    public String buildImage(BuildImageParams params, ProgressMonitor progressMonitor) throws IOException {
        try {
            return super.buildImage(params, progressMonitor);
        } catch (DockerException e) {
            throw decorateMessage(e);
        }
    }

    @Override
    public ContainerCreated createContainer(CreateContainerParams params) throws IOException {
        try {
            return super.createContainer(params);
        } catch (DockerException e) {
            // TODO fix this workaround. Is needed for https://github.com/codenvy/codenvy/issues/1215
            // and https://github.com/codenvy/codenvy/issues/2144
            if (e.getStatus() == 500 &&
                (IMAGE_NOT_FOUND_BY_SWARM_ERROR_MESSAGE.matcher(e.getOriginError()).matches() || // if swarm failed to see image
                REPOSITORY_NOT_FOUND_BY_SWARM_ERROR_MESSAGE.matcher(e.getOriginError()).matches())) { // failed to interact with repository
                try {
                    Thread.sleep(5000);                   // wait a bit
                    return super.createContainer(params); // and retry after pause
                } catch (DockerException de) {
                    throw decorateMessage(de);
                } catch (InterruptedException ie) {
                    throw decorateMessage(e);
                }
            }
            LOG.error("Failed to start {} container with {} RAM from {} image.",
                      params.getContainerName(),
                      params.getContainerConfig().getHostConfig().getMemory(),
                      params.getContainerConfig().getImage());
            throw decorateMessage(e);
        }
    }

    @Override
    public Exec createExec(CreateExecParams params) throws IOException {
        Exec exec = super.createExec(params);
        execToContainer.put(exec.getId(), params.getContainer());
        return exec;
    }

    @Override
    public void startExec(StartExecParams params, @Nullable MessageProcessor<LogMessage> execOutputProcessor)
            throws IOException {
        try {
            super.startExec(params, execOutputProcessor);
        } catch (ExecNotFoundException e) {
            // Sometimes swarm returns this error for unknown reason, see https://github.com/docker/swarm/issues/2664
            // Log additional info to find out if this endpoint knows about exec at exactly that time
            logMissingExecInfo(params.getExecId());
            try {
                // Wait in case swarm needs some time to find exec
                Thread.sleep(3000);
                super.startExec(params, execOutputProcessor);
            } catch (InterruptedException ie) {
                // throw original error
                throw new IOException(e);
            }
        } finally {
            execToContainer.invalidate(params.getExecId());
        }
    }

    private void logMissingExecInfo(String execId) {
        try {
            LOG.warn("Exec '{}' not found tracing.");
            ExecInfo execInfo = super.getExecInfo(execId);
            LOG.warn("Exec '{}' not found tracing. Info: {}", execId, execInfo);
        } catch (IOException e) {
            LOG.warn("Exec '{}' not found tracing. Exec inspection failed. Error: {}", execId, e.getMessage());
        }
        String container = execToContainer.getIfPresent(execId);
        try {
            ContainerInfo containerInfo = inspectContainer(container);
            LOG.warn("Exec '{}' not found tracing. Container info on not found exec on start:{}",
                     execId, containerInfo);
        } catch (IOException e) {
            LOG.warn("Exec '{}' not found tracing. Container inspection failed. Error: {}", execId, e.getMessage());
        }
    }

    private DockerException decorateMessage(DockerException e) {
        if (e.getOriginError() != null && e.getOriginError().contains("no resources available to schedule container")) {
            e = new DockerException("The system is out of resources. Please contact your system admin.",
                                    e.getOriginError(),
                                    e.getStatus());
        }
        return e;
    }

    /**
     * Fetches nodes from {@link SystemInfo#getDriverStatus()} which contains
     * information about all available nodes(addresses available RAM etc).
     * <pre>
     * Scheme of driver status content:
     *
     * [0] -> ["Nodes", "number of nodes"]
     * [1] -> ["hostname", "ip:port"]
     * [2] -> ["Containers", "number of containers"]
     * [3] -> ["Reserved CPUs", "number of free/reserved CPUs"]
     * [4] -> ["Reserved Memory", "number of free/reserved Memory"]
     * [5] -> ["Labels", "executiondriver=native-0.2, kernel..."]
     *
     * Example:
     *
     * [0] -> ["Nodes", "2"]
     * [1] -> ["swarm1.codenvy.com", "192.168.1.1:2375"]
     * [2] -> ["Containers", "14"]
     * [3] -> ["Reserved CPUs", "0/2"]
     * [4] -> ["Reserved Memory", "0 b / 3.79GiB"]
     * [5] -> ["Labels", "executiondriver=native-0.2, kernel..."]
     * [6] -> ["swarm2.codenvy.com", "192.168.1.2:2375"]
     * [7] -> ["Containers", "9"]
     * [8] -> ["Reserved CPUs", "0/2"]
     * [9] -> ["Reserved Memory", "0 b / 3.79GiB"]
     * [10] -> ["Labels", "executiondriver=native-0.2, kernel..."]
     * </pre>
     */
    public List<DockerNode> getAvailableNodes() throws IOException {
        SystemInfo systemInfo = getSystemInfo();
        final String[][] systemDescription = systemInfo.getSystemStatus() != null ? systemInfo.getSystemStatus()
                                                                                  : systemInfo.getDriverStatus();
        if (systemDescription == null) {
            throw new DockerException("Can't find available docker nodes. DriverStatus, SystemStatus fields missing.", 500);
        }
        int count = 0;
        int startsFrom = 0;
        for (int i = 0; i < systemDescription.length; ++i) {
            if ("Nodes".equals(Strings.nullToEmpty(systemDescription[i][0]).trim())) {
                count = firstNonNull(tryParse(systemDescription[i][1]), 0);
                startsFrom = i + 1;
                break;
            }
        }
        final ArrayList<DockerNode> nodes = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            final String[] node = systemDescription[i * nodeDescriptionLength + startsFrom];
            nodes.add(new DockerNode(node[0], node[1]));
        }
        return nodes;
    }

    //TODO find better solution
    private URI addrToUri(String addr) {
        return URI.create(nodeDaemonScheme + "://" + addr);
    }
}
