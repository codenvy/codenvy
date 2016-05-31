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
package com.codenvy.router;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.MachineRegistry;
import org.eclipse.che.api.machine.server.wsagent.WsAgentLauncher;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Extends {@link org.eclipse.che.api.machine.server.MachineManager} with methods that returns instance with direct servers urls.
 * Is used to avoid routing requests from {@link org.eclipse.che.api.machine.server.proxy.MachineExtensionProxyServlet}
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class RouterMachineManager extends MachineManager {
    private final SnapshotDao              snapshotDao;
    private final MachineRegistry          machineRegistry;
    private final MachineInstanceProviders machineInstanceProviders;
    private final String                   machineLogsDir;
    private final EventService             eventService;
    private final int                      defaultMachineMemorySizeMB;
    private final RouterRulesRegistry      routerRulesRegistry;
    private final WsAgentLauncher          wsAgentLauncher;

    @Inject
    public RouterMachineManager(SnapshotDao snapshotDao,
                                MachineRegistry machineRegistry,
                                MachineInstanceProviders machineInstanceProviders,
                                @Named("machine.logs.location") String machineLogsDir,
                                EventService eventService,
                                @Named("machine.default_mem_size_mb") int defaultMachineMemorySizeMB,
                                RouterRulesRegistry routerRulesRegistry,
                                WsAgentLauncher wsAgentLauncher) {
        super(snapshotDao,
              machineRegistry,
              machineInstanceProviders,
              machineLogsDir,
              eventService,
              defaultMachineMemorySizeMB,
              wsAgentLauncher);
        this.snapshotDao = snapshotDao;
        this.machineRegistry = machineRegistry;
        this.machineInstanceProviders = machineInstanceProviders;
        this.machineLogsDir = machineLogsDir;
        this.eventService = eventService;
        this.defaultMachineMemorySizeMB = defaultMachineMemorySizeMB;
        this.routerRulesRegistry = routerRulesRegistry;
        this.wsAgentLauncher = wsAgentLauncher;
    }

    /**
     * Is used to create self-wrapping implementations
     */
    RouterMachineManager(RouterMachineManager manager) {
        this(manager.snapshotDao,
             manager.machineRegistry,
             manager.machineInstanceProviders,
             manager.machineLogsDir,
             manager.eventService,
             manager.defaultMachineMemorySizeMB,
             manager.routerRulesRegistry,
             manager.wsAgentLauncher);
    }

    private Map<String, ServerImpl> rewriteServersUrls(String machineId, Map<String, ? extends Server> servers) {
        Map<String, ServerImpl> serversWithRewrittenUrls = new HashMap<>();
        // returns only tcp ports which are used in servers map without '/tcp' suffix
        for (RoutingRule routingRule : routerRulesRegistry.getRules(machineId)) {
            // suppose there is only 1 rule and we use it
            String[] routingRuleAddress = routingRule.getUri().split(":", 2);

            final String exposedPort = Integer.toString(routingRule.getExposedPort());
            final Server serverWithRealAddress = servers.get(exposedPort + "/tcp");

            String routedUrl = null;
            if (serverWithRealAddress.getUrl() != null) {
                routedUrl = UriBuilder.fromUri(serverWithRealAddress.getUrl())
                                      .host(routingRuleAddress[0])
                                      .port(routingRuleAddress.length == 1 ? -1 : Integer.valueOf(routingRuleAddress[1]))
                                      .build()
                                      .toString();
            }

            serversWithRewrittenUrls.put(exposedPort + "/tcp", new ServerImpl(serverWithRealAddress.getRef(),
                                                                              serverWithRealAddress.getProtocol(),
                                                                              routingRule.getUri(),
                                                                              serverWithRealAddress.getPath(),
                                                                              routedUrl));
        }

        // add those servers that do not have mappings in RouterRulesRegistry
        servers.entrySet()
               .stream()
               .filter(serverEntry -> !serversWithRewrittenUrls.containsKey(serverEntry.getKey()))
               .forEach(serverEntry -> serversWithRewrittenUrls.put(serverEntry.getKey(), new ServerImpl(serverEntry.getValue())));

        return serversWithRewrittenUrls;
    }

    MachineImpl getDevMachineWithDirectServersUrls(String workspaceId) throws NotFoundException, MachineException {
        final MachineImpl devMachine = super.getDevMachine(workspaceId);
        return new MachineImpl(devMachine) {
            @Override
            public MachineRuntimeInfoImpl getRuntime() {
                final MachineRuntimeInfo machineRuntime = devMachine.getRuntime();
                return new MachineRuntimeInfoImpl(MachineRuntimeInfoImpl.builder()
                                                                        .setEnvVariables(machineRuntime.getEnvVariables())
                                                                        .setProperties(machineRuntime.getProperties())
                                                                        .setServers(rewriteServersUrls(devMachine.getId(),
                                                                                                       machineRuntime.getServers()))
                                                                        .build());
            }
        };
    }
}
