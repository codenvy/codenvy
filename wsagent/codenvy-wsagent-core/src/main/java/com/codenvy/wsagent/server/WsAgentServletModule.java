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
package com.codenvy.wsagent.server;

import com.google.inject.servlet.ServletModule;
import org.eclipse.che.api.core.cors.CheCorsFilter;
import org.eclipse.che.inject.DynaModule;

/**
 * General binding that may be reused by other Codenvy based basic assembly. This class add
 * additional to @{@link org.eclipse.che.wsagent.server.WsAgentServletModule} servlet bindings.
 *
 * <p>Note: bindings from @{@link org.eclipse.che.wsagent.server.CheWsAgentServletModule} are Che
 * specific and must be removed from target packaging.
 *
 * @author Sergii Kabashniuk
 * @author Max Shaposhnik
 * @author Alexander Garagatyi
 */
@DynaModule
public class WsAgentServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    // listeners
    getServletContext().addListener(new com.codenvy.auth.sso.client.DestroySessionListener());

    // filters
    filter("/*").through(CheCorsFilter.class);
    filter("/*")
        .through(org.eclipse.che.multiuser.machine.authentication.agent.MachineLoginFilter.class);

    // servlets
    install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
    serveRegex("/[^/]+/api((?!(/(ws|eventbus)($|/.*)))/.*)")
        .with(org.everrest.guice.servlet.GuiceEverrestServlet.class);
  }
}
