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

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerPropertiesImpl;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategy;
import org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategyProvider;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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
    private ServerEvaluationStrategyProvider serverEvaluationStrategyProvider;
    @Mock
    private ServerEvaluationStrategy         serverEvaluationStrategy;

    @Test
    public void shouldReturnUnchangedServersOfDockerInstanceRuntimeInfoIfNoModifiersIsProvided() throws Exception {
        HashMap<String, ServerImpl> originServers = new HashMap<>();
        originServers.put("8080/tcp", new ServerImpl("ref1",
                                                     "http",
                                                     DEFAULT_HOST + ":32000",
                                                     "http://" + DEFAULT_HOST + ":32000/some/path",
                                                     new ServerPropertiesImpl("/some/path",
                                                                              DEFAULT_HOST + ":32000",
                                                                              "http://" + DEFAULT_HOST +
                                                                              ":32000/some/path")));
        originServers.put("1000/tcp", new ServerImpl("ref2",
                                                     "wss",
                                                     DEFAULT_HOST + ":32001",
                                                     "wss://" + DEFAULT_HOST + ":32001/some/path",
                                                     new ServerPropertiesImpl("/some/path",
                                                                              DEFAULT_HOST + ":32001",
                                                                              "wss://" + DEFAULT_HOST +
                                                                              ":32001/some/path")));
        HostedServersInstanceRuntimeInfo runtimeInfo =
                prepareHostedServersInstanceRuntimeInfo(originServers,
                                                        singletonMap("otherreference",
                                                                     new UriTemplateServerProxyTransformer(
                                                                             "http://host:9090/path", null, null) {}));

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
                                                                              "http://" + DEFAULT_HOST +
                                                                              ":32000/some/path")));
        originServers.put("1000/tcp", new ServerImpl("ref2",
                                                     "wss",
                                                     DEFAULT_HOST + ":30001",
                                                     "wss://" + DEFAULT_HOST + ":32001/some/path",
                                                     new ServerPropertiesImpl("/some/path",
                                                                              DEFAULT_HOST + ":30001",
                                                                              "wss://" + DEFAULT_HOST +
                                                                              ":32001/some/path")));
        HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", originServers.get("8080/tcp"));
        expectedServers.put("1000/tcp", new ServerImpl("ref2",
                                                       "http",
                                                       "host:9090",
                                                       "http://host:9090/path",
                                                       new ServerPropertiesImpl("/path",
                                                                                "host:9090",
                                                                                "http://host:9090/path")));

        HostedServersInstanceRuntimeInfo runtimeInfo =
                prepareHostedServersInstanceRuntimeInfo(originServers,
                                                        singletonMap("ref2", new UriTemplateServerProxyTransformer(
                                                                "http://host:9090/path", null, null) {}));

        Map<String, ServerImpl> modifiedServers = runtimeInfo.getServers();

        assertEquals(modifiedServers, expectedServers);
    }

    private HostedServersInstanceRuntimeInfo prepareHostedServersInstanceRuntimeInfo(
            Map<String, ServerImpl> servers, Map<String, MachineServerProxyTransformer> modifiers) {

        HostedServersInstanceRuntimeInfo runtimeInfo = spy(new HostedServersInstanceRuntimeInfo(mock(ContainerInfo.class),
                                                                                                DEFAULT_HOST,
                                                                                                mock(MachineConfig.class),
                                                                                                emptySet(),
                                                                                                emptySet(),
                                                                                                modifiers,
                                                                                                serverEvaluationStrategyProvider));

        when(serverEvaluationStrategyProvider.get()).thenReturn(serverEvaluationStrategy);
        when(serverEvaluationStrategy.getServers(any(ContainerInfo.class), anyString(), any())).thenReturn(servers);

        return runtimeInfo;
    }
}
