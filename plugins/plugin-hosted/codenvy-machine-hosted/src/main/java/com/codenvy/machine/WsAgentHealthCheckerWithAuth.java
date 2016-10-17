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

import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;

import org.eclipse.che.api.agent.server.WsAgentHealthCheckerImpl;
import org.eclipse.che.api.agent.server.WsAgentPingRequestFactory;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

/**
 * Mechanism for checking workspace agent's state by using the machine token.
 *
 * @author Vitalii Parfonov
 * @author Valeriy Svydenko
 */
@Singleton
public class WsAgentHealthCheckerWithAuth extends WsAgentHealthCheckerImpl {

    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final String                 apiEndpoint;


    @Inject
    public WsAgentHealthCheckerWithAuth(WsAgentPingRequestFactory pingRequestFactory,
                                        HttpJsonRequestFactory httpJsonRequestFactory,
                                        @Named("che.api") String apiEndpoint) {
        super(pingRequestFactory);
        this.apiEndpoint = apiEndpoint;
        this.httpJsonRequestFactory = httpJsonRequestFactory;
    }


    // modifies the ping request if it is possible to get the machine token.
    protected HttpJsonRequest createPingRequest(Machine devMachine) throws ServerException {
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
                                                 .asDto(MachineTokenDto.class)
                                                 .getMachineToken();
        } catch (ApiException | IOException ex) {
            LOG.warn("Failed to get machine token", ex);
        }
        return machineToken == null ? pingRequest : pingRequest.setAuthorizationHeader(machineToken);
    }
}
