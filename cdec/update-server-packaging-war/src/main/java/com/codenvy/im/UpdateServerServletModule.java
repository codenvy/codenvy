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
package com.codenvy.im;

import com.codenvy.auth.sso.client.ClientUrlExtractor;
import com.codenvy.auth.sso.client.LoginFilter;
import com.codenvy.auth.sso.client.RecoverableTokenHandler;
import com.codenvy.auth.sso.client.RequestWrapper;
import com.codenvy.auth.sso.client.deploy.SsoClientServletModule;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.inject.DynaModule;
import org.everrest.guice.servlet.GuiceEverrestServlet;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** @author Anatoliy Bazko */
@DynaModule
public class UpdateServerServletModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
        bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url")).to("/site/error/error-cookies-disabled");
        bindConstant().annotatedWith(Names.named("auth.sso.client_skip_filter_regexp")).to(".*((/repository/(updates|properties|public/download|log-event)/)"
                                                                                           + "|(/report/parameters)"
                                                                                           + "|(/util/client-ip)).*");

        bind(com.codenvy.auth.sso.client.WebAppClientUrlExtractor.class);
        bind(com.codenvy.auth.sso.client.token.ChainedTokenExtractor.class);
        bind(com.codenvy.auth.sso.client.filter.RequestFilter.class).to(com.codenvy.auth.sso.client.filter.RegexpRequestFilter.class);
        bind(com.codenvy.auth.sso.client.TokenHandler.class).to(AnonymousUserTokenHandler.class);

        filterRegex("/(?!_sso/).*$").through(LoginFilter.class);
        install(new SsoClientServletModule());
        serve("/*").with(GuiceEverrestServlet.class);
    }

    public static class AnonymousUserTokenHandler extends RecoverableTokenHandler {

        @Inject
        public AnonymousUserTokenHandler(RequestWrapper requestWrapper,
                                         ClientUrlExtractor clientUrlExtractor,
                                         @Named("auth.sso.client_allow_anonymous") boolean allowAnonymous) {
            super(requestWrapper, clientUrlExtractor, allowAnonymous);
        }

        @Override public void handleMissingToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
            if (!"GET".equals(request.getMethod())) {
                //go with anonymous
                EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
                environmentContext.setSubject(Subject.ANONYMOUS);
                chain.doFilter(requestWrapper.wrapRequest(request.getSession(), request, Subject.ANONYMOUS), response);
            } else {
                super.handleMissingToken(request, response, chain);
            }
        }
    }
}
