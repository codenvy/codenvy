/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.machine.agent.launcher;

import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.agent.WsAgentLauncher;
import org.eclipse.che.api.agent.server.WsAgentPingRequestFactory;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.environment.server.MachineProcessManager;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Starts the ws-agent and pings it using custom machine request, until ws-agent sends appropriate
 * event about start.
 *
 * @author Anton Korneta
 * @author Anatolii Bazko
 */
@Singleton
public class WsAgentWithAuthLauncherImpl extends WsAgentLauncher {

  private final HttpJsonRequestFactory httpJsonRequestFactory;
  private final String apiEndpoint;

  @Inject
  public WsAgentWithAuthLauncherImpl(
      Provider<MachineProcessManager> machineProcessManagerProvider,
      HttpJsonRequestFactory httpJsonRequestFactory,
      WsAgentPingRequestFactory wsAgentPingRequestFactory,
      @Nullable @Named("machine.ws_agent.run_command") String wsAgentRunCommand,
      @Named("che.workspace.agent.dev.max_start_time_ms") long wsAgentMaxStartTimeMs,
      @Named("che.workspace.agent.dev.ping_delay_ms") long wsAgentPingDelayMs,
      @Named("che.workspace.agent.dev.ping_timeout_error_msg") String pingTimedOutErrorMessage,
      @Named("che.api") String apiEndpoint) {
    super(
        machineProcessManagerProvider,
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
    final String tokenServiceUrl =
        UriBuilder.fromUri(apiEndpoint)
            .replacePath("api/machine/token/" + devMachine.getWorkspaceId())
            .build()
            .toString();
    String machineToken = null;
    try {
      machineToken =
          httpJsonRequestFactory
              .fromUrl(tokenServiceUrl)
              .setMethod(HttpMethod.GET)
              .request()
              .asDto(MachineTokenDto.class)
              .getMachineToken();
    } catch (ApiException | IOException ex) {
      LOG.warn("Failed to get machine token", ex);
    }
    return machineToken == null ? pingRequest : pingRequest.setAuthorizationHeader(machineToken);
  }
}
