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
package com.codenvy.api.deploy;

import com.codenvy.auth.sso.client.CodenvyCsrfFilter;
import com.google.common.base.Joiner;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import org.apache.catalina.filters.CorsFilter;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.swagger.deploy.BasicSwaggerConfigurationModule;
import org.everrest.websockets.WSConnectionTracker;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.apache.catalina.filters.CorsFilter.DEFAULT_ALLOWED_ORIGINS;

/**
 * Servlet module composer for api war.
 */
@DynaModule
public class OnPremisesIdeApiServletModule extends ServletModule {
    public static final List<String> pathForLoginFilter = asList("/factory/*",
                                                                 "/activity/*",
                                                                 "/workspace/*",
                                                                 "/user/*",
                                                                 "/admin/user",
                                                                 "/admin/user/*",
                                                                 "/factory",
                                                                 "/workspace",
                                                                 "/audit",
                                                                 "/user",
                                                                 "/profile",
                                                                 "/profile/*",
                                                                 "/oauth/token",
                                                                 "/oauth/authenticate",
                                                                 "/oauth/1.0/authenticate",
                                                                 "/oauth/1.0/authorization",
                                                                 "/machine/token/*",
                                                                 "/recipe",
                                                                 "/recipe/*",
                                                                 "/stack",
                                                                 "/stack/*",
                                                                 "/creditcard/*",
                                                                 "/ssh/*",
                                                                 "/ssh",
                                                                 "/nodes",
                                                                 "/nodes/*",
                                                                 "/permissions",
                                                                 "/permissions/*",
                                                                 "/preferences",
                                                                 "/preferences/*",
                                                                 "/license/system",
                                                                 "/license/system/*",
                                                                 "/ldap/sync",
                                                                 "/ldap/sync/*",
                                                                 "/organization",
                                                                 "/organization/*",
                                                                 "/system/*",
                                                                 "/license/account/*",
                                                                 "/resource/*",
                                                                 "/invite",
                                                                 "/invite/*");

    @Override
    protected void configureServlets() {
        filter(pathForLoginFilter).through(com.codenvy.auth.sso.client.LoginFilter.class);

        // comma separated list of paths like "/service1,/service2"
        bindConstant().annotatedWith(Names.named("csrf_filter.paths_accepting_parameters")).to("/ssh");
        filter(pathForLoginFilter).through(CodenvyCsrfFilter.class);

        filter(pathForLoginFilter).through(com.codenvy.api.license.filter.SystemLicenseLoginFilter.class);

        final Map<String, String> corsFilterParams = new HashMap<>();
        corsFilterParams.put("cors.allowed.origins", DEFAULT_ALLOWED_ORIGINS);
        corsFilterParams.put("cors.allowed.methods", "GET," +
                                                     "POST," +
                                                     "HEAD," +
                                                     "OPTIONS," +
                                                     "PUT," +
                                                     "DELETE");
        corsFilterParams.put("cors.allowed.headers", "Content-Type," +
                                                     "X-Requested-With," +
                                                     "accept," +
                                                     "Origin," +
                                                     "Access-Control-Request-Method," +
                                                     "Access-Control-Request-Headers");
        corsFilterParams.put("cors.support.credentials", "true");
        // preflight cache is available for 10 minutes
        corsFilterParams.put("cors.preflight.maxage", "10");
        bind(CorsFilter.class).in(Singleton.class);
        filter("/*").through(CorsFilter.class, corsFilterParams);

        bind(com.codahale.metrics.servlets.ThreadDumpServlet.class).in(Singleton.class);
        bind(com.codahale.metrics.servlets.PingServlet.class).in(Singleton.class);
        serve("/metrics/ping").with(com.codahale.metrics.servlets.PingServlet.class);
        serve("/metrics/threaddump").with(com.codahale.metrics.servlets.ThreadDumpServlet.class);

        serve("/oauth").with(com.codenvy.auth.sso.oauth.OAuthLoginServlet.class);
        filter("/oauth").through(com.codenvy.auth.sso.oauth.OauthLoginFilter.class);
        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
        serveRegex("^((?!(\\/(ws|eventbus|websocket)($|\\/.*)))\\/.*)").with(org.everrest.guice.servlet.GuiceEverrestServlet.class);

        getServletContext().addListener(new WSConnectionTracker());
        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
        install(new BasicSwaggerConfigurationModule());

    }
}
