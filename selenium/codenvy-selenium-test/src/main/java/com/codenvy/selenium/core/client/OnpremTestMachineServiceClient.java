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
package com.codenvy.selenium.core.client;

import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.selenium.core.client.TestMachineServiceClient;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;

/**
 * @author Musienko Maxim
 */
@Singleton
public class OnpremTestMachineServiceClient implements TestMachineServiceClient {
    private final String                 apiEndpoint;
    private final HttpJsonRequestFactory requestFactory;

    @Inject
    public OnpremTestMachineServiceClient(TestApiEndpointUrlProvider apiEndpointProvider,
                                          HttpJsonRequestFactory requestFactory) {
        this.apiEndpoint = apiEndpointProvider.get().toString();
        this.requestFactory = requestFactory;
    }

    /**
     * Returns machine token for current workspace
     *
     * @param workspaceId
     *         the workspace id
     * @param authToken
     *         the authorization token
     * @return the machine token for current workspace
     */
    @Override
    public String getMachineApiToken(String workspaceId, String authToken) throws Exception {
        HttpJsonResponse response = requestFactory.fromUrl(apiEndpoint + "/machine/token/" + workspaceId)
                                                  .setAuthorizationHeader(authToken)
                                                  .useGetMethod()
                                                  .request();
        return response.asDto(MachineTokenDto.class).getMachineToken();
    }
}

