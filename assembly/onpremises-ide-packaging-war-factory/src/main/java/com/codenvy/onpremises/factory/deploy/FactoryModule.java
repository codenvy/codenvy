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
package com.codenvy.onpremises.factory.deploy;

import com.codenvy.api.permission.server.HttpPermissionCheckerImpl;
import com.codenvy.api.permission.server.PermissionChecker;
import com.codenvy.auth.sso.client.TokenHandler;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import org.eclipse.che.inject.DynaModule;

/**
 * Guice container configuration file. Replaces old REST application composers and servlet context listeners.
 *
 * @author Alexander Garagatyi
 */
@DynaModule
public class FactoryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PermissionChecker.class).to(HttpPermissionCheckerImpl.class);
        bind(TokenHandler.class).to(com.codenvy.api.permission.server.PermissionTokenHandler.class);
        bind(TokenHandler.class).annotatedWith(Names.named("delegated.handler"))
                                .to(com.codenvy.auth.sso.client.RecoverableTokenHandler.class);

        bindConstant().annotatedWith(Names.named("auth.sso.client_skip_filter_regexp")).to("^/factory/(_sso|resources|metrics)/(.+)$");
        bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
        bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url")).to("/site/error/error-cookies-disabled");

        bind(com.codenvy.auth.sso.client.filter.RequestFilter.class).to(com.codenvy.auth.sso.client.filter.RegexpRequestFilter.class);
    }


}
