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
package com.codenvy.onpremises;

import com.codenvy.auth.sso.client.LoginFilter;
import com.codenvy.auth.sso.client.WebAppClientUrlExtractor;
import com.codenvy.auth.sso.client.deploy.SsoClientServletModule;
import com.codenvy.auth.sso.client.token.ChainedTokenExtractor;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import javax.inject.Singleton;
import org.eclipse.che.inject.DynaModule;

/** Servlet module composer for site war. */
@DynaModule
public class OnPremisesIdeWebsiteServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
    bindConstant()
        .annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
        .to("/site/error/error-cookies-disabled");
    bindConstant()
        .annotatedWith(Names.named("pagehider.exclude.regexppattern"))
        .to("^/(metrics/|zendesk)(.*)$");

    bind(WebAppClientUrlExtractor.class);
    bind(ChainedTokenExtractor.class);
    bind(com.codenvy.auth.sso.client.TokenHandler.class)
        .to(com.codenvy.auth.sso.client.RecoverableTokenHandler.class);

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
