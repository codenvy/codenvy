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
package com.codenvy.machine.launcher;

import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;

import org.eclipse.che.api.agent.server.WsAgentPingRequestFactory;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.environment.server.MachineProcessManager;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.workspace.server.launcher.WsAgentLauncherImpl;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

/**
 * Starts the ws-agent and pings it using custom machine request,
 * until ws-agent sends appropriate event about start.
 *
 * @author Anton Korneta
 * @author Anatolii Bazko
 */
@Singleton
public class WsAgentWithAuthLauncherImpl extends WsAgentLauncherImpl {

    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final String                 apiEndpoint;

    @Inject
    public WsAgentWithAuthLauncherImpl(Provider<MachineProcessManager> machineProcessManagerProvider,
                                       HttpJsonRequestFactory httpJsonRequestFactory,
                                       WsAgentPingRequestFactory wsAgentPingRequestFactory,
                                       @Nullable @Named("machine.ws_agent.run_command") String wsAgentRunCommand,
                                       @Named("che.workspace.agent.dev.max_start_time_ms") long wsAgentMaxStartTimeMs,
                                       @Named("che.workspace.agent.dev.ping_delay_ms") long wsAgentPingDelayMs,
                                       @Named("che.workspace.agent.dev.ping_timeout_error_msg") String pingTimedOutErrorMessage,
                                       @Named("che.api") String apiEndpoint) {
        super(machineProcessManagerProvider,
              wsAgentPingRequestFactory,
              wsAgentRunCommand,
              wsAgentMaxStartTimeMs,
              wsAgentPingDelayMs,
              pingTimedOutErrorMessage);
        this.apiEndpoint = apiEndpoint;
        this.httpJsonRequestFactory = httpJsonRequestFactory;
    }

    // modifies the ping request if it is possible to get the machine token.
    @Override
    protected HttpJsonRequest createPingRequest(Instance devMachine) throws ServerException {
        final HttpJsonRequest pingRequest = super.createPingRequest(devMachine);
        final String tokenServiceUrl = UriBuilder.fromUri(apiEndpoint)
                                                 .replacePath("api/machine/token/" + devMachine.getWorkspaceId())
                                                 .build()
                                                 .toString();
        String machineToken = null;
        try {
            machineToken = httpJsonRequestFactory.fromUrl(tokenServiceUrl)
                                                 .setMethod(HttpMethod.GET)
                                                 .request()
                                                 .asDto(MachineTokenDto.class).getMachineToken();
        } catch (ApiException | IOException ex) {
            LOG.warn("Failed to get machine token", ex);
        }
        return machineToken == null ? pingRequest : pingRequest.setAuthorizationHeader(machineToken);
    }
}
