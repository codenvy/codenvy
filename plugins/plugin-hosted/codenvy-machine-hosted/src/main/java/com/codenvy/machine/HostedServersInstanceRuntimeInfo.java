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

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo;
import org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategyProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Rewrites machine servers to proxy all requests to them.
 *
 * @author Alexander Garagatyi
 */
public class HostedServersInstanceRuntimeInfo extends DockerInstanceRuntimeInfo {
    private final Map<String, MachineServerProxyTransformer> transformers;

    private Map<String, ServerImpl> servers;

    @Inject
    public HostedServersInstanceRuntimeInfo(@Assisted ContainerInfo containerInfo,
                                            @Assisted String containerInternalHostname,
                                            @Assisted MachineConfig machineConfig,
                                            @Named("machine.docker.dev_machine.machine_servers") Set<ServerConf> devMachineServers,
                                            @Named("machine.docker.machine_servers") Set<ServerConf> allMachinesServers,
                                            Map<String, MachineServerProxyTransformer> transformers,
                                            ServerEvaluationStrategyProvider serverEvaluationStrategyProvider) {
        super(containerInfo,
              machineConfig,
              containerInternalHostname,
              serverEvaluationStrategyProvider,
              devMachineServers,
              allMachinesServers);
        this.transformers = transformers;
    }

    @Override
    public Map<String, ServerImpl> getServers() {
        // don't use locks because value is always the same and it is ok if one thread overrides saved value
        if (servers == null) {
            // get servers with direct urls, transform them to use proxy if needed
            final HashMap<String, ServerImpl> servers = new HashMap<>(super.getServers());
            for (Map.Entry<String, ServerImpl> serverEntry : servers.entrySet()) {
                if (transformers.containsKey(serverEntry.getValue().getRef())) {
                    serverEntry.setValue(transformers.get(serverEntry.getValue().getRef())
                                                     .transform(serverEntry.getValue()));
                }
            }
            this.servers = servers;
        }

        return servers;
    }
}
