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
package com.codenvy.machine;

import com.google.inject.assistedinject.Assisted;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo;
import org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategyProvider;

/**
 * Rewrites machine servers to proxy all requests to them.
 *
 * @author Alexander Garagatyi
 */
public class HostedServersInstanceRuntimeInfo extends DockerInstanceRuntimeInfo {
  private final Map<String, MachineServerProxyTransformer> transformers;

  private Map<String, ServerImpl> servers;

  @Inject
  public HostedServersInstanceRuntimeInfo(
      @Assisted ContainerInfo containerInfo,
      @Assisted String containerInternalHostname,
      @Assisted MachineConfig machineConfig,
      @Named("machine.docker.dev_machine.machine_servers") Set<ServerConf> devMachineServers,
      @Named("machine.docker.machine_servers") Set<ServerConf> allMachinesServers,
      Map<String, MachineServerProxyTransformer> transformers,
      ServerEvaluationStrategyProvider serverEvaluationStrategyProvider) {
    super(
        containerInfo,
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
          serverEntry.setValue(
              transformers.get(serverEntry.getValue().getRef()).transform(serverEntry.getValue()));
        }
      }
      this.servers = servers;
    }

    return servers;
  }
}
