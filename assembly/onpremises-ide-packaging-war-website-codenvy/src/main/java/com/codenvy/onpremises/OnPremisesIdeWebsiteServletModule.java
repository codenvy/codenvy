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
package com.codenvy.onpremises;


import com.codenvy.auth.sso.client.LoginFilter;
import com.codenvy.auth.sso.client.WebAppClientUrlExtractor;
import com.codenvy.auth.sso.client.deploy.SsoClientServletModule;
import com.codenvy.auth.sso.client.token.ChainedTokenExtractor;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import org.eclipse.che.inject.DynaModule;

import javax.inject.Singleton;

/** Servlet module composer for site war. */

@DynaModule

public class OnPremisesIdeWebsiteServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
        bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
                      .to("/site/error/error-cookies-disabled");
        bindConstant().annotatedWith(Names.named("pagehider.exclude.regexppattern")).to("^/(metrics/|zendesk)(.*)$");

        bind(WebAppClientUrlExtractor.class);
        bind(ChainedTokenExtractor.class);
        bind(com.codenvy.auth.sso.client.TokenHandler.class).to(com.codenvy.auth.sso.client.RecoverableTokenHandler.class);

        bind(com.codahale.metrics.servlets.ThreadDumpServlet.class).in(Singleton.class);
        bind(com.codahale.metrics.servlets.PingServlet.class).in(Singleton.class);
        bind(com.xemantic.tadedon.servlet.CacheDisablingFilter.class).in(Singleton.class);

        filter("/*").through(com.xemantic.tadedon.servlet.CacheDisablingFilter.class);
        filter("/private/*", "/zendesk").through(LoginFilter.class);
        filter("/*").through(PagesExtensionHider.class);
        serve("/metrics/ping").with(com.codahale.metrics.servlets.PingServlet.class);
        serve("/metrics/threaddump").with(com.codahale.metrics.servlets.ThreadDumpServlet.class);
        serve("/zendesk").with(ZendeskRedirectServlet.class);

        install(new SsoClientServletModule());
    }
}
