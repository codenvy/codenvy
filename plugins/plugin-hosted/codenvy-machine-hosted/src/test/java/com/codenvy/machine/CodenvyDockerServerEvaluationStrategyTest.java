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

import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.NetworkSettings;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class CodenvyDockerServerEvaluationStrategyTest {
    private static final String HOST = "test.host.com";

    @Mock
    private ContainerInfo   containerInfo;
    @Mock
    private NetworkSettings networkSettings;

    private CodenvyDockerServerEvaluationStrategy strategy = new CodenvyDockerServerEvaluationStrategy();

    @BeforeMethod
    public void setUp() throws Exception {
        when(containerInfo.getNetworkSettings()).thenReturn(networkSettings);
        when(networkSettings.getPorts()).thenReturn(emptyMap());
    }

    @Test
    public void shouldReturnEmptyMapOnRetrievalOfInternalAddressesIfNoPortExposed() throws Exception {
        assertTrue(strategy.getInternalAddressesAndPorts(containerInfo, HOST).isEmpty());
    }

    @Test
    public void shouldReturnEmptyMapOnRetrievalOfExternalAddressesIfNoPortExposed() throws Exception {
        assertTrue(strategy.getExternalAddressesAndPorts(containerInfo, HOST).isEmpty());
    }

    @Test
    public void shouldUseProvidedInternalHostOnRetrievalOfInternalAddresses() throws Exception {
        Map<String, List<PortBinding>> exposedPorts = new HashMap<>();
        exposedPorts.put("8080/tcp", singletonList(new PortBinding().withHostIp("127.0.0.1").withHostPort("32789")));
        exposedPorts.put("9090/udp", singletonList(new PortBinding().withHostIp("192.168.0.1").withHostPort("20000")));
        when(networkSettings.getPorts()).thenReturn(exposedPorts);

        Map<String, String> internalAddressesAndPorts =
                strategy.getInternalAddressesAndPorts(containerInfo, HOST);

        for (Map.Entry<String, List<PortBinding>> entry : exposedPorts.entrySet()) {
            assertEquals(internalAddressesAndPorts.get(entry.getKey()),
                         HOST + ":" + entry.getValue().get(0).getHostPort());
        }
    }

    @Test
    public void shouldUseProvidedInternalHostOnRetrievalOfExternalAddresses() throws Exception {
        Map<String, List<PortBinding>> exposedPorts = new HashMap<>();
        exposedPorts.put("8080/tcp", singletonList(new PortBinding().withHostIp("127.0.0.1").withHostPort("32789")));
        exposedPorts.put("9090/udp", singletonList(new PortBinding().withHostIp("192.168.0.1").withHostPort("20000")));
        when(networkSettings.getPorts()).thenReturn(exposedPorts);

        Map<String, String> externalAddressesAndPorts =
                strategy.getExternalAddressesAndPorts(containerInfo, HOST);

        for (Map.Entry<String, List<PortBinding>> entry : exposedPorts.entrySet()) {
            assertEquals(externalAddressesAndPorts.get(entry.getKey()),
                         HOST + ":" + entry.getValue().get(0).getHostPort());
        }
    }
}
