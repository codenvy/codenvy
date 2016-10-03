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

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerPropertiesImpl;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.NetworkSettings;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class HostedServersInstanceRuntimeInfoTest {
    private static final String DEFAULT_HOST = "default-host.com";

    @Mock
    private ContainerInfo   containerInfo;
    @Mock
    private NetworkSettings networkSettings;
    @Mock
    private MachineConfig   machineConfig;
    @Mock
    private ContainerConfig containerConfig;

    private HostedServersInstanceRuntimeInfo runtimeInfo;

    @Test
    public void shouldReturnUnchangedServersOfDockerInstanceRuntimeInfoIfNoModifiersIsProvided() throws Exception {
        HashMap<String, ServerImpl> originServers = new HashMap<>();
        originServers.put("8080/tcp", new ServerImpl("ref1",
                                                     "http",
                                                     DEFAULT_HOST + ":32000",
                                                     "http://" + DEFAULT_HOST + ":32000/some/path",
                                                     new ServerPropertiesImpl("/some/path",
                                                                              DEFAULT_HOST + ":32000",
                                                                              "http://" + DEFAULT_HOST + ":32000/some/path")));
        originServers.put("1000/tcp", new ServerImpl("ref2",
                                                     "wss",
                                                     DEFAULT_HOST + ":32001",
                                                     "wss://" + DEFAULT_HOST + ":32001/some/path",
                                                     new ServerPropertiesImpl("/some/path",
                                                                              DEFAULT_HOST + ":32001",
                                                                              "wss://" + DEFAULT_HOST + ":32001/some/path")));
        makeParentOfHostedRuntimeInfoReturnServers(originServers, singletonMap("otherreference",
                                                                               new UriTemplateServerProxyTransformer("http://host:9090/path") {}));

        Map<String, ServerImpl> modifiedServers = runtimeInfo.getServers();

        assertEquals(modifiedServers, originServers);
    }

    @Test
    public void shouldModifyOnlyServersThatHasCorrespondingModifier() throws Exception {
        HashMap<String, ServerImpl> originServers = new HashMap<>();
        originServers.put("8080/tcp", new ServerImpl("ref1",
                                                     "http",
                                                     DEFAULT_HOST + ":32000",
                                                     "http://" + DEFAULT_HOST + ":32000/some/path",
                                                     new ServerPropertiesImpl("/some/path",
                                                                              DEFAULT_HOST + ":32000",
                                                                              "http://" + DEFAULT_HOST + ":32000/some/path")));
        originServers.put("1000/tcp", new ServerImpl("ref2",
                                                     "wss",
                                                     DEFAULT_HOST + ":30001",
                                                     "wss://" + DEFAULT_HOST + ":32001/some/path",
                                                     new ServerPropertiesImpl("/some/path",
                                                                              DEFAULT_HOST + ":30001",
                                                                              "wss://" + DEFAULT_HOST + ":32001/some/path")));
        HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", originServers.get("8080/tcp"));
        expectedServers.put("1000/tcp", new ServerImpl("ref2",
                                                       "http",
                                                       "host:9090",
                                                       "http://host:9090/path",
                                                       new ServerPropertiesImpl("/path",
                                                                                "host:9090",
                                                                                "http://host:9090/path")));

        makeParentOfHostedRuntimeInfoReturnServers(expectedServers, singletonMap("ref2",
                                                                                 new UriTemplateServerProxyTransformer("http://host:9090/path") {}));

        Map<String, ServerImpl> modifiedServers = runtimeInfo.getServers();

        assertEquals(modifiedServers, expectedServers);
    }

    private void makeParentOfHostedRuntimeInfoReturnServers(Map<String, ServerImpl> servers,
                                                            Map<String, MachineServerProxyTransformer> modifiers) {
        Set<ServerConf> serverConfigs = new HashSet<>();
        HashMap<String, List<PortBinding>> exposedPorts = new HashMap<>();
        for (Map.Entry<String, ServerImpl> serverEntry : servers.entrySet()) {
            ServerImpl server = serverEntry.getValue();
            serverConfigs.add(new ServerConfImpl(server.getRef(),
                                                 serverEntry.getKey(),
                                                 server.getProtocol(),
                                                 server.getProperties().getPath()));

            exposedPorts.put(serverEntry.getKey(),
                             singletonList(new PortBinding().withHostPort(server.getAddress().split(":")[1])));
        }

        runtimeInfo = spy(new HostedServersInstanceRuntimeInfo(containerInfo,
                                                               null,
                                                               DEFAULT_HOST,
                                                               machineConfig,
                                                               emptySet(),
                                                               serverConfigs,
                                                               modifiers));

        when(containerInfo.getNetworkSettings()).thenReturn(networkSettings);
        when(networkSettings.getPorts()).thenReturn(exposedPorts);
        when(containerInfo.getConfig()).thenReturn(containerConfig);
        when(containerConfig.getLabels()).thenReturn(Collections.emptyMap());
    }
}
