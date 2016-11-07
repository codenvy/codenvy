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
package com.codenvy.machine.agent;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.agent.server.launcher.AgentLauncher;
import org.eclipse.che.api.agent.shared.model.Agent;

/**
 * @author Alexander Garagatyi
 */
public class CodenvyAgentModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(org.eclipse.che.api.agent.server.WsAgentHealthChecker.class)
                .to(com.codenvy.machine.WsAgentHealthCheckerWithAuth.class);

        Multibinder<AgentLauncher> agentLaunchers = Multibinder.newSetBinder(binder(), AgentLauncher.class);
        agentLaunchers.addBinding().to(com.codenvy.machine.agent.launcher.WsAgentWithAuthLauncherImpl.class);
        agentLaunchers.addBinding().to(com.codenvy.machine.agent.launcher.RsyncAgentLauncherImpl.class);

        Multibinder<Agent> agentsMultibinder = Multibinder.newSetBinder(binder(), Agent.class);
        agentsMultibinder.addBinding().to(com.codenvy.machine.agent.RsyncAgent.class);

        bind(String.class).annotatedWith(Names.named("workspace.backup.public_key"))
                          .toProvider(com.codenvy.machine.agent.WorkspaceSyncPublicKeyProvider.class);
    }
}
