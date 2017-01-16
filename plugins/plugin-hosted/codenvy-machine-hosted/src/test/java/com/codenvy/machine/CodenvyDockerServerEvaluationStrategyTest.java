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
