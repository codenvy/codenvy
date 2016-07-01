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
package com.codenvy.swarm.client;

import com.codenvy.swarm.client.model.DockerNode;
import com.google.common.base.Strings;

import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.exception.DockerException;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.SystemInfo;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.PullParams;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.primitives.Ints.tryParse;

/**
 * Swarm implementation of {@link DockerConnector} that can be used on distributed system
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class SwarmDockerConnector extends DockerConnector {

    private final NodeSelectionStrategy   strategy;
    //TODO should it be done in other way?
    private final String                  nodeDaemonScheme;
    private final int                     nodeDescriptionLength;

    @Inject
    public SwarmDockerConnector(DockerConnectorConfiguration connectorConfiguration,
                                DockerConnectionFactory connectionFactory,
                                DockerRegistryAuthResolver authManager,
                                @Named("swarm.client.node_description_length") int nodeDescriptionLength) {
        super(connectorConfiguration, connectionFactory, authManager);
        this.nodeDescriptionLength = nodeDescriptionLength;
        this.strategy = new RandomNodeSelectionStrategy();
        this.nodeDaemonScheme = "http";
    }

    @Override
    public void pull(PullParams params, ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        final DockerNode node = strategy.select(getAvailableNodes());
        super.pull(params, progressMonitor, addrToUri(node.getAddr()));
    }

    @Override
    public String buildImage(BuildImageParams params, ProgressMonitor progressMonitor)
            throws IOException, InterruptedException {
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
            throw decorateMessage(e);
        }
    }

    private DockerException decorateMessage(DockerException e) {
        if (e.getOriginError().contains("no resources available to schedule container")) {
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
    private List<DockerNode> getAvailableNodes() throws IOException {
        final String[][] systemStatus = getSystemInfo().getSystemStatus();
        if (systemStatus == null) {
            throw new DockerException("Can't find available docker nodes. DriverStatus, SystemStatus fields missing.", 500);
        }
        int count = 0;
        int startsFrom = 0;
        for (int i = 0; i < systemStatus.length; ++i) {
            if ("Nodes".equals(Strings.nullToEmpty(systemStatus[i][0]).trim())) {
                count = firstNonNull(tryParse(systemStatus[i][1]), 0);
                startsFrom = i + 1;
                break;
            }
        }
        final ArrayList<DockerNode> nodes = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            final String[] node = systemStatus[i * nodeDescriptionLength + startsFrom];
            nodes.add(new DockerNode(node[0], node[1]));
        }
        return nodes;
    }

    //TODO find better solution
    private URI addrToUri(String addr) {
        return URI.create(nodeDaemonScheme + "://" + addr);
    }
}
