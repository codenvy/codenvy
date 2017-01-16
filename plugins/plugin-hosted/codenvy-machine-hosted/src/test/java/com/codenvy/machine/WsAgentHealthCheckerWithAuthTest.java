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

import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;

import org.eclipse.che.api.agent.server.WsAgentPingRequestFactory;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WS_AGENT_PORT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Valeriy Svydenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WsAgentHealthCheckerWithAuthTest {
    private final static String WS_AGENT_SERVER_URL = "ws_agent";
    private final static String API_ENDPOINT        = "api_endpoint";

    @Mock
    private HttpJsonRequestFactory    httpJsonRequestFactory;
    @Mock
    private WsAgentPingRequestFactory wsAgentPingRequestFactory;
    @Mock
    private Machine                   devMachine;
    @Mock
    private MachineRuntimeInfo        machineRuntimeInfo;
    @Mock
    private Server                    server;
    @Mock
    private HttpJsonRequest           httpJsonRequest;
    @Mock
    private HttpJsonResponse          httpJsonResponse;

    private Map<String, Server> servers = new HashMap<>(1);

    private WsAgentHealthCheckerWithAuth checker;

    @BeforeMethod
    public void setUp() throws Exception {
        servers.put(WSAGENT_REFERENCE, server);
        servers.put(WS_AGENT_PORT, server);

        when(server.getRef()).thenReturn(WSAGENT_REFERENCE);
        when(server.getUrl()).thenReturn(WS_AGENT_SERVER_URL);

        checker = new WsAgentHealthCheckerWithAuth(wsAgentPingRequestFactory, httpJsonRequestFactory, API_ENDPOINT);

        when(httpJsonRequestFactory.fromUrl(anyString())).thenReturn(httpJsonRequest);
        when(wsAgentPingRequestFactory.createRequest(devMachine)).thenReturn(httpJsonRequest);
        when(httpJsonRequest.setMethod(any())).thenReturn(httpJsonRequest);
        when(httpJsonRequest.setTimeout(anyInt())).thenReturn(httpJsonRequest);
        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);

        when(httpJsonResponse.getResponseCode()).thenReturn(200);
        when(httpJsonResponse.asString()).thenReturn("response");

        when(devMachine.getRuntime()).thenReturn(machineRuntimeInfo);
        doReturn(servers).when(machineRuntimeInfo).getServers();
    }

    @Test
    public void stateShouldBeReturnedWithStatusNotFoundIfWorkspaceAgentIsNotExist() throws Exception {
        when(machineRuntimeInfo.getServers()).thenReturn(emptyMap());

        WsAgentHealthStateDto result = checker.check(devMachine);

        assertEquals(NOT_FOUND.getStatusCode(), result.getCode());
    }

    @Test
    public void returnStateWithNotFoundCode() throws Exception {
        doReturn(emptyMap()).when(machineRuntimeInfo).getServers();

        final WsAgentHealthStateDto check = checker.check(devMachine);
        assertEquals(NOT_FOUND.getStatusCode(), check.getCode());
        assertEquals("Workspace Agent not available", check.getReason());
    }

    @Test
    public void pingRequestToWsAgentShouldBeSent() throws Exception {
        MachineTokenDto machineTokenDto = mock(MachineTokenDto.class);
        when(httpJsonResponse.asDto(MachineTokenDto.class)).thenReturn(machineTokenDto);
        when(machineTokenDto.getMachineToken()).thenReturn("token");
        when(httpJsonRequest.setAuthorizationHeader(eq("token"))).thenReturn(httpJsonRequest);

        final WsAgentHealthStateDto result = checker.check(devMachine);

        verify(httpJsonRequest, times(2)).request();
        assertEquals(200, result.getCode());
    }

    @Test
    public void returnResultWithUnavailableStateIfDoNotGetResponseFromWsAgent() throws Exception {
        doThrow(IOException.class).when(httpJsonRequest).request();

        final WsAgentHealthStateDto result = checker.check(devMachine);

        verify(httpJsonRequest, times(2)).request();
        assertEquals(SERVICE_UNAVAILABLE.getStatusCode(), result.getCode());
    }
}

