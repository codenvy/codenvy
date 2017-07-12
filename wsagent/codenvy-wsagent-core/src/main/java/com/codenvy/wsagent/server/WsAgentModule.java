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
