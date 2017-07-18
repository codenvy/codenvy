/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.wsagent.server;

import com.codenvy.api.agent.CodenvyProjectServiceLinksInjector;
import com.codenvy.api.permission.server.PermissionChecker;
import com.codenvy.auth.sso.client.TokenHandler;
import com.codenvy.auth.sso.client.token.RequestTokenExtractor;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import org.eclipse.che.EventBusURLProvider;
import org.eclipse.che.UserTokenProvider;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.project.server.ProjectServiceLinksInjector;
import org.eclipse.che.inject.DynaModule;

/**
 * @author Evgen Vidolob
 * @author Sergii Kabashniuk
 * @author Max Shaposhnik
 * @author Alexander Garagatyi
 * @author Anton Korneta
 * @author Vitaly Parfonov
 */
@DynaModule
public class WsAgentModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(PermissionChecker.class).to(com.codenvy.api.permission.server.HttpPermissionCheckerImpl.class);
        bind(TokenHandler.class).to(com.codenvy.api.permission.server.PermissionTokenHandler.class);
        bind(TokenHandler.class).annotatedWith(Names.named("delegated.handler"))
                                .to(com.codenvy.auth.sso.client.NoUserInteractionTokenHandler.class);

        bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
                      .to("/site/error/error-cookies-disabled");
        bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
        bind(ProjectServiceLinksInjector.class).to(CodenvyProjectServiceLinksInjector.class);
        bind(HttpJsonRequestFactory.class).to(AuthorizeTokenHttpJsonRequestFactory.class);
        bind(RequestTokenExtractor.class).to(com.codenvy.auth.sso.client.token.ChainedTokenExtractor.class);


        bind(WsAgentAnalyticsAddresser.class);

        bind(String.class).annotatedWith(Names.named("user.token")).toProvider(UserTokenProvider.class);
        bind(String.class).annotatedWith(Names.named("event.bus.url")).toProvider(EventBusURLProvider.class);
        bind(String.class).annotatedWith(Names.named("wsagent.endpoint")).toProvider(com.codenvy.api.agent.WsAgentURLProvider.class);


    }
}
