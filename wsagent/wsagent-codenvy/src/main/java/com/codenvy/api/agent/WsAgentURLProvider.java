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
package com.codenvy.api.agent;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Collection;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;

/**
 * Provides URL to workspace agent inside container.
 *
 * @author Anton Korneta
 */
public class WsAgentURLProvider implements Provider<String> {
    private static final Logger LOG = LoggerFactory.getLogger(WsAgentURLProvider.class);

    private final String                 wsId;
    private final String                 workspaceApiEndpoint;
    private final String                 machineToken;
    private final HttpJsonRequestFactory requestFactory;

    private String cachedAgentUrl;

    @Inject
    public WsAgentURLProvider(@Named("che.api") String apiEndpoint,
                              @Named("env.USER_TOKEN") String machineToken,
                              @Named("env.CHE_WORKSPACE_ID") String wsId,
                              HttpJsonRequestFactory requestFactory) {
        this.wsId = wsId;
        this.machineToken = machineToken;
        this.workspaceApiEndpoint = apiEndpoint + "/workspace/";
        this.requestFactory = requestFactory;
    }

    @Override
    public String get() {
        if (isNullOrEmpty(cachedAgentUrl)) {
            try {
                final WorkspaceDto workspace = requestFactory.fromUrl(workspaceApiEndpoint + wsId)
                                                             .useGetMethod()
                                                             .request()
                                                             .asDto(WorkspaceDto.class);
                if (workspace.getRuntime() != null) {
                    final Collection<ServerDto> servers = workspace.getRuntime()
                                                                   .getDevMachine()
                                                                   .getRuntime()
                                                                   .getServers()
                                                                   .values();
                    for (ServerDto server : servers) {
                        if (WSAGENT_REFERENCE.equals(server.getRef())) {
                            cachedAgentUrl = UriBuilder.fromUri(server.getUrl())
                                                       .queryParam("token", machineToken)
                                                       .build()
                                                       .toString();
                            return cachedAgentUrl;
                        }
                    }
                }
            } catch (ApiException | IOException ex) {
                LOG.warn(ex.getLocalizedMessage());
                throw new RuntimeException("Failed to configure wsagent endpoint");
            }
        }
        return cachedAgentUrl;
    }
}
