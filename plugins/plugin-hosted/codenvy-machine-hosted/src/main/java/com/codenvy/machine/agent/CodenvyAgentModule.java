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
package com.codenvy.machine.agent;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.eclipse.che.api.agent.server.launcher.AgentLauncher;
import org.eclipse.che.api.agent.shared.model.Agent;

/** @author Alexander Garagatyi */
public class CodenvyAgentModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(org.eclipse.che.api.agent.server.AgentRegistryService.class);

    bind(org.eclipse.che.api.agent.server.WsAgentHealthChecker.class)
        .to(com.codenvy.machine.WsAgentHealthCheckerWithAuth.class);

    bind(org.eclipse.che.api.agent.server.AgentRegistry.class)
        .to(org.eclipse.che.api.agent.server.impl.AgentRegistryImpl.class);

    bindConstant()
        .annotatedWith(Names.named("machine.terminal_agent.run_command"))
        .to(
            "$HOME/che/terminal/che-websocket-terminal "
                + "-addr :4411 "
                + "-cmd ${SHELL_INTERPRETER} "
                + "-path '/[^/]+' "
                + "-enable-auth "
                + "-enable-activity-tracking");
    bindConstant()
        .annotatedWith(Names.named("machine.exec_agent.run_command"))
        .to(
            "$HOME/che/exec-agent/che-exec-agent "
                + "-addr :4412 "
                + "-cmd ${SHELL_INTERPRETER} "
                + "-path '/[^/]+' "
                + "-enable-auth "
                + "-logs-dir $HOME/che/exec-agent/logs");

    Multibinder<AgentLauncher> launchers = Multibinder.newSetBinder(binder(), AgentLauncher.class);
    launchers.addBinding().to(com.codenvy.machine.agent.launcher.WsAgentWithAuthLauncherImpl.class);
    launchers
        .addBinding()
        .to(com.codenvy.machine.agent.launcher.MachineInnerRsyncAgentLauncherImpl.class);
    launchers.addBinding().to(org.eclipse.che.api.agent.ExecAgentLauncher.class);
    launchers.addBinding().to(org.eclipse.che.api.agent.TerminalAgentLauncher.class);
    launchers.addBinding().to(org.eclipse.che.api.agent.SshAgentLauncher.class);

    Multibinder<Agent> agents = Multibinder.newSetBinder(binder(), Agent.class);
    agents.addBinding().to(com.codenvy.machine.agent.MachineInnerRsyncAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.SshAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.UnisonAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.ExecAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.TerminalAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.WsAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.LSPhpAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.LSPythonAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.LSJsonAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.LSCSharpAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.LSTypeScriptAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.GitCredentialsAgent.class);

    bind(org.eclipse.che.plugin.machine.ssh.exec.SshMachineExecAgentLauncher.class);
    bind(String.class)
        .annotatedWith(Names.named("workspace.backup.public_key"))
        .toProvider(com.codenvy.machine.agent.WorkspaceSyncPublicKeyProvider.class);
  }
}
