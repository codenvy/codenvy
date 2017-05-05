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
package com.codenvy.wsagent.server;


import com.google.inject.servlet.ServletModule;

import org.eclipse.che.api.core.cors.CheCorsFilter;
import org.eclipse.che.inject.DynaModule;

/**
 * General binding that may be reused by other Codenvy based basic assembly.
 * This class add additional to @{@link org.eclipse.che.wsagent.server.WsAgentServletModule}
 * servlet bindings.
 * <p>
 * Note: bindings from @{@link org.eclipse.che.wsagent.server.CheWsAgentServletModule} are Che specific
 * and must be removed from target packaging.
 *
 * @author Sergii Kabashniuk
 * @author Max Shaposhnik
 * @author Alexander Garagatyi
 */
@DynaModule
public class WsAgentServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        //listeners
        getServletContext().addListener(new com.codenvy.auth.sso.client.DestroySessionListener());

        //filters
        filter("/*").through(CheCorsFilter.class);
        filter("/*").through(com.codenvy.machine.authentication.agent.MachineLoginFilter.class);
        filter("/*").through(com.codenvy.workspace.LastAccessTimeFilter.class);

        //servlets
        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
        serveRegex("/[^/]+/api((?!(/(ws|eventbus)($|/.*)))/.*)").with(org.everrest.guice.servlet.GuiceEverrestServlet.class);

    }
}
