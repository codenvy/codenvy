/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
