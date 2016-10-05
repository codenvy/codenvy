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
package com.codenvy.machine.authentication.server;


import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;
import com.google.common.collect.ImmutableList;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.environment.server.MachineServiceLinksInjector;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.WorkspaceServiceLinksInjector;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_WEBSOCKET_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_STOP_WORKSPACE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Helps to inject {@link WorkspaceService} related links.
 *
 * @author Anton Korneta
 */
public class WorkspaceServiceAuthLinksInjector extends WorkspaceServiceLinksInjector {
    private static final Logger LOG                  = LoggerFactory.getLogger(WorkspaceServiceAuthLinksInjector.class);
    private static final String MACHINE_TOKEN        = "token";
    private static final String MACHINE_SERVICE_PATH = "/machine/token/";

    private final String                 tokenServiceBaseUrl;
    private final HttpJsonRequestFactory httpJsonRequestFactory;

    @Inject
    public WorkspaceServiceAuthLinksInjector(@Named("che.api") String apiEndpoint,
                                             HttpJsonRequestFactory httpJsonRequestFactory,
                                             MachineServiceLinksInjector machineLinksInjector) {
        super(machineLinksInjector);
        this.tokenServiceBaseUrl = apiEndpoint + MACHINE_SERVICE_PATH;
        this.httpJsonRequestFactory = httpJsonRequestFactory;
    }

    protected void injectRuntimeLinks(WorkspaceDto workspace, URI ideUri, UriBuilder uriBuilder, ServiceContext serviceContext) {
        final WorkspaceRuntimeDto runtime = workspace.getRuntime();
        // add links for running workspace
        if (workspace.getStatus() == RUNNING && runtime != null) {
            runtime.getLinks()
                   .add(createLink("DELETE",
                                   uriBuilder.clone()
                                             .path(WorkspaceService.class, "stop")
                                             .build(workspace.getId())
                                             .toString(),
                                   LINK_REL_STOP_WORKSPACE));
            String token = null;
            try {
                token = httpJsonRequestFactory.fromUrl(tokenServiceBaseUrl + workspace.getId())
                                              .setMethod(HttpMethod.GET)
                                              .request()
                                              .asDto(MachineTokenDto.class).getMachineToken();
            } catch (ApiException | IOException ex) {
                LOG.warn("Failed to get machine token", ex);
            }
            final String machineToken = firstNonNull(token, "");

            runtime.getMachines().forEach(machine -> injectMachineLinks(machine, serviceContext));

            final MachineDto devMachine = runtime.getDevMachine();
            if (devMachine != null) {
                final Collection<ServerDto> servers = devMachine.getRuntime()
                                                                .getServers()
                                                                .values();
                servers.stream()
                       .filter(server -> WSAGENT_REFERENCE.equals(server.getRef()))
                       .findAny()
                       .ifPresent(wsAgent -> {
                           runtime.getLinks()
                                  .add(createLink("GET",
                                                  wsAgent.getUrl(),
                                                  WSAGENT_REFERENCE));
                           runtime.getLinks()
                                  .add(createLink("GET",
                                                  UriBuilder.fromUri(wsAgent.getUrl())
                                                            .path("ws")
                                                            .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                                                            .build()
                                                            .toString(),
                                                  WSAGENT_WEBSOCKET_REFERENCE));

                           devMachine.getLinks()
                                     .add(createLink("GET",
                                                     UriBuilder.fromUri(wsAgent.getUrl())
                                                               .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                                                               .path("/ws")
                                                               .queryParam(MACHINE_TOKEN, machineToken)
                                                               .build()
                                                               .toString(),
                                                     WSAGENT_WEBSOCKET_REFERENCE,
                                                     ImmutableList.of(newDto(LinkParameter.class).withName(MACHINE_TOKEN)
                                                                                                 .withDefaultValue(machineToken)
                                                                                                 .withRequired(true))));
                       });

                servers.stream()
                       .filter(server -> TERMINAL_REFERENCE.equals(server.getRef()))
                       .findAny()
                       .ifPresent(terminal -> devMachine.getLinks()
                                                        .add(createLink("GET",
                                                                        UriBuilder.fromUri(terminal.getUrl())
                                                                                  .scheme("https".equals(ideUri.getScheme()) ? "wss"
                                                                                                                             : "ws")
                                                                                  .queryParam(MACHINE_TOKEN, machineToken)
                                                                                  .path("/pty")
                                                                                  .build()
                                                                                  .toString(),
                                                                        TERMINAL_REFERENCE)));
            }
        }
    }
}
