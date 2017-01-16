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

import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategy;

import java.util.List;
import java.util.Map;

/**
 * Represents server evaluation strategy for Codenvy. By default, calling
 * {@link ServerEvaluationStrategy#getServers(ContainerInfo, String, Map)} will return a completed
 * {@link ServerImpl} with internal and external address set to the address of the Docker host.
 *
 * @author Alexander Garagatyi
 * @see ServerEvaluationStrategy
 */
public class CodenvyDockerServerEvaluationStrategy extends ServerEvaluationStrategy {
    @Override
    protected Map<String, String> getInternalAddressesAndPorts(ContainerInfo containerInfo, String internalHost) {
        Map<String, List<PortBinding>> portBindings = containerInfo.getNetworkSettings().getPorts();

        return getExposedPortsToAddressPorts(internalHost, portBindings);
    }

    @Override
    protected Map<String, String> getExternalAddressesAndPorts(ContainerInfo containerInfo, String internalHost) {
        return getInternalAddressesAndPorts(containerInfo, internalHost);
    }
}
