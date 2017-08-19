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
package com.codenvy.onpremises.deploy;

import com.codenvy.api.permission.server.PermissionChecker;
import com.codenvy.auth.sso.client.TokenHandler;
import com.codenvy.onpremises.FactoryRedirectServlet;
import com.codenvy.onpremises.maintenance.MaintenanceStatusServlet;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import javax.inject.Singleton;
import org.eclipse.che.inject.DynaModule;

/** Servlet module composer for user dashboard war. */
@DynaModule
public class DashboardServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(PermissionChecker.class)
        .to(com.codenvy.api.permission.server.HttpPermissionCheckerImpl.class);
    bind(TokenHandler.class).to(com.codenvy.api.permission.server.PermissionTokenHandler.class);
    bind(TokenHandler.class)
        .annotatedWith(Names.named("delegated.handler"))
        .to(com.codenvy.auth.sso.client.RecoverableTokenHandler.class);

    bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
    bindConstant()
        .annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
        .to("/site/error/error-cookies-disabled");

    bind(com.xemantic.tadedon.servlet.CacheForcingFilter.class).in(Singleton.class);

    filterRegex("/(?!assets/).*$").through(com.xemantic.tadedon.servlet.CacheForcingFilter.class);

    filterRegex("/(?!_sso/).*$").through(com.codenvy.auth.sso.client.LoginFilter.class);

    serve("/scheduled").with(MaintenanceStatusServlet.class);
    serve("/factory").with(FactoryRedirectServlet.class);

    install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
  }
}
