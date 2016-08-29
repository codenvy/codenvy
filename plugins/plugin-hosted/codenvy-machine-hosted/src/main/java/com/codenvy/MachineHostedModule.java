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
package com.codenvy;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.agent.server.wsagent.WsAgentLauncher;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;

public class MachineHostedModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(org.eclipse.che.plugin.docker.client.DockerConnector.class).to(com.codenvy.swarm.client.SwarmDockerConnector.class);
        bind(org.eclipse.che.plugin.docker.client.DockerRegistryDynamicAuthResolver.class)
                .to(com.codenvy.auth.aws.ecr.AwsEcrAuthResolver.class);

        bind(String.class).annotatedWith(Names.named("machine.docker.machine_env"))
                          .toProvider(com.codenvy.machine.MaintenanceConstraintProvider.class);

        install(new org.eclipse.che.plugin.docker.machine.ext.DockerTerminalModule());
        bind(org.eclipse.che.api.agent.server.terminal.MachineTerminalLauncher.class);

        install(new org.eclipse.che.plugin.docker.machine.proxy.DockerProxyModule());

        install(new FactoryModuleBuilder()
                        .implement(org.eclipse.che.api.machine.server.spi.Instance.class,
                                   org.eclipse.che.plugin.docker.machine.DockerInstance.class)
                        .implement(org.eclipse.che.api.machine.server.spi.InstanceProcess.class,
                                   org.eclipse.che.plugin.docker.machine.DockerProcess.class)
                        .implement(org.eclipse.che.plugin.docker.machine.node.DockerNode.class,
                                   com.codenvy.machine.RemoteDockerNode.class)
                        .implement(org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo.class,
                                   com.codenvy.machine.HostedServersInstanceRuntimeInfo.class)
                        .build(org.eclipse.che.plugin.docker.machine.DockerMachineFactory.class));

        install(new org.eclipse.che.plugin.docker.machine.ext.DockerExtServerModule());

        bind(String.class).annotatedWith(Names.named("machine.docker.che_api.endpoint"))
                          .to(Key.get(String.class, Names.named("api.endpoint")));

        // install(new com.codenvy.router.MachineRouterModule());

        bind(org.eclipse.che.api.workspace.server.event.MachineStateListener.class).asEagerSingleton();

        install(new org.eclipse.che.plugin.docker.machine.DockerMachineModule());
        Multibinder<InstanceProvider> machineImageProviderMultibinder =
                Multibinder.newSetBinder(binder(), org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
        machineImageProviderMultibinder.addBinding()
                                       .to(com.codenvy.machine.HostedDockerInstanceProvider.class);
        bind(WsAgentLauncher.class).to(com.codenvy.machine.launcher.WsAgentWithAuthLauncherImpl.class);

        install(new com.codenvy.machine.interceptor.MachineHostedInterceptorModule());
    }

}
