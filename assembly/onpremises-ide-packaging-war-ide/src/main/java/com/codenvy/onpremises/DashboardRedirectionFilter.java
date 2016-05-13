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
package com.codenvy.onpremises;

import com.codenvy.auth.sso.client.ServerClient;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.auth.sso.client.token.RequestTokenExtractor;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.inject.DynaModule;

import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Redirect user to dashboard if request wasn't made to project in ws or to temporary ws
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class DashboardRedirectionFilter implements Filter {
    private static Pattern projectPattern = Pattern.compile("^/ws/[^/]+?/.+?");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp = (HttpServletResponse)response;
        EnvironmentContext context = EnvironmentContext.getCurrent();

        if ("GET".equals(req.getMethod())
            && !projectPattern.matcher(req.getRequestURI()).matches()
            && !context.isWorkspaceTemporary()
            && context.getSubject().getUserId() != null
            && req.getQueryString() == null
                ) {
            resp.sendRedirect("/dashboard/");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    /**
     * @author Alexander Garagatyi
     */
    @DynaModule
    public static class IdeModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(com.codenvy.service.http.WorkspaceInfoCache.WorkspaceCacheLoader.class)
                    .to(com.codenvy.service.http.WorkspaceInfoCache.HttpWorkspaceCacheLoader.class);

            bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
            bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url")).to("/site/error/error-cookies-disabled");
            bindConstant().annotatedWith(Names.named("error.page.workspace_not_found_redirect_url")).to("/site/error/error-tenant-name");
            bindConstant().annotatedWith(Names.named("auth.sso.client_skip_filter_regexp")).to("^/ws/_sso/(.+)$");

            bind(RequestTokenExtractor.class).to(com.codenvy.auth.sso.client.token.ChainedTokenExtractor.class);
            bind(com.codenvy.auth.sso.client.SSOContextResolver.class).to(com.codenvy.auth.sso.client.EnvironmentContextResolver.class);
            bind(ServerClient.class).to(com.codenvy.auth.sso.client.HttpSsoServerClient.class);
            bind(RequestFilter.class).to(com.codenvy.auth.sso.client.filter.RegexpRequestFilter.class);
        }
    }

    /**
     * Servlet module composer for ide war.
     *
     * @author Alexander Garagatyi
     */
    @DynaModule
    public static class IdeServletModule extends ServletModule {
        @Override
        protected void configureServlets() {
            filter("/*").through(com.codenvy.auth.sso.client.LoginFilter.class);
            install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
        }
    }
}
