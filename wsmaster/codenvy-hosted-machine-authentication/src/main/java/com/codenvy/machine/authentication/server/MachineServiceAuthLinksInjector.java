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
import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.environment.server.MachineService;
import org.eclipse.che.api.environment.server.MachineServiceLinksInjector;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE;

/**
 * Helps to inject {@link MachineService} related links.
 *
 * @author Anton Korneta
 */
public class MachineServiceAuthLinksInjector extends MachineServiceLinksInjector {
    private static final Logger LOG                  = LoggerFactory.getLogger(MachineServiceAuthLinksInjector.class);
    private static final String MACHINE_SERVICE_PATH = "/machine/token/";

    private final String                 tokenServiceBaseUrl;
    private final HttpJsonRequestFactory httpJsonRequestFactory;

    @Inject
    public MachineServiceAuthLinksInjector(@Named("che.api") String apiEndpoint,
                                           HttpJsonRequestFactory httpJsonRequestFactory) {
        this.tokenServiceBaseUrl = apiEndpoint + MACHINE_SERVICE_PATH;
        this.httpJsonRequestFactory = httpJsonRequestFactory;
    }

    @VisibleForTesting
    protected void injectTerminalLink(MachineDto machine, ServiceContext serviceContext, List<Link> links) {
        if (machine.getRuntime() != null) {
            String token = null;
            try {
                token = httpJsonRequestFactory.fromUrl(tokenServiceBaseUrl + machine.getWorkspaceId())
                                              .setMethod(HttpMethod.GET)
                                              .request()
                                              .asDto(MachineTokenDto.class)
                                              .getMachineToken();
            } catch (ApiException | IOException ex) {
                LOG.warn("Failed to get machine token", ex);
            }
            final String machineToken = firstNonNull(token, "");
            final String scheme = serviceContext.getBaseUriBuilder().build().getScheme();
            final Collection<ServerDto> servers = machine.getRuntime().getServers().values();
            servers.stream()
                   .filter(server -> TERMINAL_REFERENCE.equals(server.getRef()))
                   .findAny()
                   .ifPresent(terminal -> links.add(createLink("GET",
                                                               UriBuilder.fromUri(terminal.getUrl())
                                                                         .scheme("https".equals(scheme) ? "wss"
                                                                                                        : "ws")
                                                                         .queryParam("token", machineToken)
                                                                         .path("/pty")
                                                                         .build()
                                                                         .toString(),
                                                               TERMINAL_REFERENCE)));
        }
    }
}
