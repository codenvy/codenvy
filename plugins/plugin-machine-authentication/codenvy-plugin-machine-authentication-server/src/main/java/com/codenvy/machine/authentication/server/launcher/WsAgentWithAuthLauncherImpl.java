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
package com.codenvy.machine.authentication.server.launcher;

import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.wsagent.WsAgentLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Starts the ws-agent and pings it using custom machine request,
 * until ws-agent sends appropriate event about start.
 *
 * @author Anton Korneta
 */
@Singleton
public class WsAgentWithAuthLauncherImpl implements WsAgentLauncher {
    public static final String WS_AGENT_PROCESS_START_COMMAND = "machine.ws_agent.run_command";
    public static final String WS_AGENT_PROCESS_NAME          = "CheWsAgent";
    public static final String WS_AGENT_PORT                  = "4401/tcp";

    private static final Logger LOG                             = LoggerFactory.getLogger(WsAgentWithAuthLauncherImpl.class);
    private static final String WS_AGENT_PROCESS_OUTPUT_CHANNEL = "workspace:%s:ext-server:output";

    private final int                      wsAgentPingConnectionTimeoutMs;
    private final long                     wsAgentMaxStartTimeMs;
    private final long                     wsAgentPingDelayMs;
    private final String                   wsAgentStartCommandLine;
    private final String                   wsAgentPingPath;
    private final String                   apiEndpoint;
    private final String                   pingTimedOutErrorMessage;
    private final Provider<MachineManager> machineManagerProvider;
    private final HttpJsonRequestFactory   httpJsonRequestFactory;

    @Inject
    public WsAgentWithAuthLauncherImpl(@Named("machine.ws_agent.ping_conn_timeout_ms") int wsAgentPingConnectionTimeoutMs,
                                       @Named("machine.ws_agent.max_start_time_ms") long wsAgentMaxStartTimeMs,
                                       @Named("machine.ws_agent.ping_delay_ms") long wsAgentPingDelayMs,
                                       @Named(WS_AGENT_PROCESS_START_COMMAND) String wsAgentStartCommandLine,
                                       @Named("machine.ws_agent.ping_timed_out_error_msg") String pingTimedOutErrorMessage,
                                       @Named("machine.ws_agent.agent_api.path") String wsAgentApiPath,
                                       @Named("api.endpoint") String apiEndpoint,
                                       Provider<MachineManager> machineManagerProvider,
                                       HttpJsonRequestFactory httpJsonRequestFactory) {
        this.wsAgentPingConnectionTimeoutMs = wsAgentPingConnectionTimeoutMs;
        this.wsAgentMaxStartTimeMs = wsAgentMaxStartTimeMs;
        this.wsAgentPingDelayMs = wsAgentPingDelayMs;
        this.wsAgentStartCommandLine = wsAgentStartCommandLine;
        this.pingTimedOutErrorMessage = pingTimedOutErrorMessage;
        this.machineManagerProvider = machineManagerProvider;
        this.httpJsonRequestFactory = httpJsonRequestFactory;

        // everrest respond 404 to path to rest without trailing slash
        this.wsAgentPingPath = wsAgentApiPath;
        this.apiEndpoint = apiEndpoint;
    }

    public static String getWsAgentProcessOutputChannel(String workspaceId) {
        return String.format(WS_AGENT_PROCESS_OUTPUT_CHANNEL, workspaceId);
    }

    @Override
    public void startWsAgent(String workspaceId) throws NotFoundException, MachineException, InterruptedException {
        final Machine devMachine = getMachineManager().getDevMachine(workspaceId);
        try {
            getMachineManager().exec(devMachine.getId(),
                                     new CommandImpl(WS_AGENT_PROCESS_NAME, wsAgentStartCommandLine, "Arbitrary"),
                                     getWsAgentProcessOutputChannel(workspaceId));
            final HttpJsonRequest wsAgentPingRequest = createPingRequest(devMachine);
            long pingStartTimestamp = System.currentTimeMillis();
            LOG.debug("Starts pinging ws agent. Workspace ID:{}. Url:{}. Timestamp:{}",
                      workspaceId,
                      wsAgentPingRequest,
                      pingStartTimestamp);

            while (System.currentTimeMillis() - pingStartTimestamp < wsAgentMaxStartTimeMs) {
                if (pingWsAgent(wsAgentPingRequest)) {
                    return;
                } else {
                    Thread.sleep(wsAgentPingDelayMs);
                }
            }
        } catch (BadRequestException wsAgentLaunchingExc) {
            throw new MachineException(wsAgentLaunchingExc.getLocalizedMessage(), wsAgentLaunchingExc);
        }
        throw new MachineException(pingTimedOutErrorMessage);
    }

    private HttpJsonRequest createPingRequest(Machine devMachine) throws NotFoundException {
        final String machineToken = getMachineToken(devMachine.getWorkspaceId()).getMachineToken();
        final String wsAgentPingUrl = UriBuilder.fromUri(devMachine.getRuntime()
                                                                   .getServers()
                                                                   .get(WS_AGENT_PORT)
                                                                   .getUrl())
                                                .replacePath(wsAgentPingPath)
                                                .build()
                                                .toString();
        return httpJsonRequestFactory.fromUrl(wsAgentPingUrl)
                                     .setAuthorizationHeader("Bearer " + machineToken)
                                     .setMethod(HttpMethod.GET)
                                     .setTimeout(wsAgentPingConnectionTimeoutMs);
    }

    private boolean pingWsAgent(HttpJsonRequest wsAgentPingRequest) throws MachineException {
        try {
            final HttpJsonResponse pingResponse = wsAgentPingRequest.request();
            if (pingResponse.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
        } catch (ApiException | IOException ignored) {
        }
        return false;
    }

    private MachineManager getMachineManager() {
        return machineManagerProvider.get();
    }

    private MachineTokenDto getMachineToken(String wsId) throws NotFoundException {
        final String tokenServiceUrl = UriBuilder.fromUri(apiEndpoint)
                                                 .replacePath("api/machine/token/" + wsId)
                                                 .build()
                                                 .toString();
        final MachineTokenDto machineToken;
        try {
            machineToken = httpJsonRequestFactory.fromUrl(tokenServiceUrl)
                                                 .setMethod(HttpMethod.GET)
                                                 .request()
                                                 .asDto(MachineTokenDto.class);
        } catch (ApiException | IOException ignored) {
            throw new NotFoundException("Machine token for workspace " + wsId + " not found");
        }
        return machineToken;
    }
}
