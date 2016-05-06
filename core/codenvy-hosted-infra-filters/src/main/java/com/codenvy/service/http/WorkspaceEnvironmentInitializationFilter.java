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
package com.codenvy.service.http;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.commons.env.EnvironmentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Sets workspace meta information into current {@link EnvironmentContext}
 *
 * @author Sergii Kabashniuk
 * @author Eugene Voevodin
 */
public abstract class WorkspaceEnvironmentInitializationFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceEnvironmentInitializationFilter.class);

    @Inject
    @Named("error.page.workspace_not_found_redirect_url")
    private String wsNotFoundRedirectUrl;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            final Workspace workspace = getWorkspaceFromRequest(request);
            if (workspace != null) {
                final EnvironmentContext env = EnvironmentContext.getCurrent();
                env.setWorkspaceName(workspace.getConfig().getName());
                env.setWorkspaceId(workspace.getId());
                env.setWorkspaceTemporary(workspace.isTemporary());
                LOG.debug("Set environment context - workspace name: {}, workspace id: {}, account id:{} , is temporary: {}",
                          env.getWorkspaceName(),
                          env.getWorkspaceId(),
                          env.isWorkspaceTemporary());
                chain.doFilter(request, response);
            } else {
                workspaceNotFoundHandler(request, response, chain);
            }
        } finally {
            EnvironmentContext.reset();
        }
    }

    /**
     * Retrieves {@link Workspace} from {@code request} if possible, otherwise returns {@code null}
     * <p/>
     * When method returns {@code null} then {@link #workspaceNotFoundHandler(ServletRequest, ServletResponse, FilterChain)}
     * will be invoked with default behaviour, if default behaviour is not appropriate - override handler method as well
     *
     * @param request
     *         current filter request
     * @return workspace descriptor or {@code null}
     */
    protected abstract Workspace getWorkspaceFromRequest(ServletRequest request);

    /**
     * Will be invoked when {@link #getWorkspaceFromRequest(ServletRequest)} returns {@code null}.
     * By default sends redirect to {@link #wsNotFoundRedirectUrl}
     *
     * @param request
     *         current filter request
     * @param response
     *         current filter response
     * @param chain
     *         chain for current filter
     * @throws IOException
     *         when any i/o error occurs
     * @throws ServletException
     *         when any other error occurs
     */
    protected void workspaceNotFoundHandler(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                                                                                                                        ServletException {
        ((HttpServletResponse)response).sendRedirect(wsNotFoundRedirectUrl);
    }

    @Override
    public void destroy() {
    }
}
