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
package com.codenvy.api.deploy;

import com.google.inject.servlet.ServletModule;

import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.swagger.deploy.BasicSwaggerConfigurationModule;
import org.everrest.websockets.WSConnectionTracker;

import javax.inject.Singleton;

/**
 * Servlet module composer for api war.
 */
@DynaModule
public class OnPremisesIdeApiServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        filter("/project/*",
               "/maven/*",
               "/ws/*",
               "/code-assistant-java/*",
               "/java-name-environment/*",
               "/debug-java/*",
               "/async/*",
               "/git/*",
               "/svn/*",
               "/invite/*",
               "/ssh-keys/*",
               "/appengine/*",
               "/gae-validator/*",
               "/gae-parameters/*")
                .through(com.codenvy.service.http.WorkspaceIdEnvironmentInitializationFilter.class);
        filterRegex("^/workspace/(?!config$|runtime$|.*:.*$).+")
                .through(com.codenvy.service.http.WorkspaceIdEnvironmentInitializationFilter.class);
        filter("/factory/*",
               "/activity/*",
               "/workspace/*",
               "/account/*",
               "/java-name-environment/*",
               "/user/*",
               "/admin/user",
               "/admin/user/*",
               "/analytics/*",
               "/invite/*",
               "/factory",
               "/workspace",
               "/account",
               "/user",
               "/git/*",
               "/svn/*",
               "/github/*",
               "/bitbucket/*",
               "/ssh-keys/*",
               "/async/*",
               "/internal/convert/*",
               "/profile",
               "/profile/*",
               "/analytics",
               "/oauth/token",
               "/oauth/authenticate",
               "/password/change",
               "/runner/*",
               "/builder/*",
               "/admin/runner/*",
               "/admin/builder/*",
               "/admin/plan",
               "/project/*",
               "/maven/*",
               "/ws/*",
               "/appengine/*",
               "/gae-validator/*",
               "/gae-parameters/*",
               "/billing/*",
               "/creditcard/*",
               "/invoice/*",
               "/billing/*",
               "/machine/*",
               "/machine",
               "/recipe",
               "/recipe/*",
               "/stack",
               "/stack/*",
               "/command",
               "/command/*",
               "/subscription/*",
               "/subscription",
               "/saas/*",
               "/promotion/*",
               "/resources/*",
               "/ext/*",
               "/ssh/*",
               "/ssh",
               "/permissions",
               "/permissions/*")
                .through(com.codenvy.auth.sso.client.LoginFilter.class);

        bind(com.codahale.metrics.servlets.ThreadDumpServlet.class).in(Singleton.class);
        bind(com.codahale.metrics.servlets.PingServlet.class).in(Singleton.class);
        serve("/metrics/ping").with(com.codahale.metrics.servlets.PingServlet.class);
        serve("/metrics/threaddump").with(com.codahale.metrics.servlets.ThreadDumpServlet.class);

        serve("/oauth").with(com.codenvy.auth.sso.oauth.OAuthLoginServlet.class);
        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
        serveRegex("^((?!(\\/(ws|eventbus)($|\\/.*)))\\/.*)").with(org.everrest.guice.servlet.GuiceEverrestServlet.class);

        getServletContext().addListener(new WSConnectionTracker());
        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
        install(new BasicSwaggerConfigurationModule());

    }
}
