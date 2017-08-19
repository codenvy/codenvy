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
import com.codenvy.auth.sso.client.ServerClient;
import com.codenvy.auth.sso.client.TokenHandler;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.auth.sso.client.token.RequestTokenExtractor;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.eclipse.che.inject.DynaModule;

/** @author Alexander Garagatyi */
@DynaModule
public class IdeModule extends AbstractModule {
  @Override
  protected void configure() {

    bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
    bindConstant()
        .annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
        .to("/site/error/error-cookies-disabled");
    bindConstant()
        .annotatedWith(Names.named("error.page.workspace_not_found_redirect_url"))
        .to("/site/error/error-tenant-name");
    bindConstant()
        .annotatedWith(Names.named("auth.sso.client_skip_filter_regexp"))
        .to("^/_sso/(.+)$");

    bind(RequestTokenExtractor.class)
        .to(com.codenvy.auth.sso.client.token.ChainedTokenExtractor.class);
    bind(PermissionChecker.class)
        .to(com.codenvy.api.permission.server.HttpPermissionCheckerImpl.class);
    bind(TokenHandler.class).to(com.codenvy.api.permission.server.PermissionTokenHandler.class);
    bind(TokenHandler.class)
        .annotatedWith(Names.named("delegated.handler"))
        .to(com.codenvy.auth.sso.client.RecoverableTokenHandler.class);

    bind(ServerClient.class).to(com.codenvy.auth.sso.client.HttpSsoServerClient.class);
    bind(RequestFilter.class).to(com.codenvy.auth.sso.client.filter.RegexpRequestFilter.class);
  }
}
