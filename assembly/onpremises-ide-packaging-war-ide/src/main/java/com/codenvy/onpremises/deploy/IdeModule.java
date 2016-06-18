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
package com.codenvy.onpremises.deploy;

import com.codenvy.api.permission.server.PermissionChecker;
import com.codenvy.auth.sso.client.ServerClient;
import com.codenvy.auth.sso.client.TokenHandler;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.auth.sso.client.token.RequestTokenExtractor;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import org.eclipse.che.inject.DynaModule;

/**
 * @author Alexander Garagatyi
 */
@DynaModule
public class IdeModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(com.codenvy.service.http.WorkspaceInfoCache.WorkspaceCacheLoader.class)
                .to(com.codenvy.service.http.WorkspaceInfoCache.HttpWorkspaceCacheLoader.class);

        bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
        bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url")).to("/site/error/error-cookies-disabled");
        bindConstant().annotatedWith(Names.named("error.page.workspace_not_found_redirect_url")).to("/site/error/error-tenant-name");
        bindConstant().annotatedWith(Names.named("auth.sso.client_skip_filter_regexp")).to("^/_sso/(.+)$");
        bindConstant().annotatedWith(Names.named("auth.sso.client_allow_anonymous")).to(false);

        bind(RequestTokenExtractor.class).to(com.codenvy.auth.sso.client.token.ChainedTokenExtractor.class);
        bind(PermissionChecker.class).to(com.codenvy.api.permission.server.HttpPermissionCheckerImpl.class);
        bind(TokenHandler.class).to(com.codenvy.api.permission.server.PermissionTokenHandler.class);
        bind(TokenHandler.class).annotatedWith(Names.named("delegated.handler"))
                                .to(com.codenvy.auth.sso.client.RecoverableTokenHandler.class);

        bind(ServerClient.class).to(com.codenvy.auth.sso.client.HttpSsoServerClient.class);
        bind(RequestFilter.class).to(com.codenvy.auth.sso.client.filter.RegexpRequestFilter.class);
    }
}
