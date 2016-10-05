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
package com.codenvy.machine.authentication.agent;

import com.codenvy.auth.sso.client.token.RequestTokenExtractor;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Protects user's machine from unauthorized access.
 *
 * @author Anton Korneta
 */
@Singleton
public class MachineLoginFilter implements Filter {

    private final String                 tokenServiceEndpoint;
    private final HttpJsonRequestFactory requestFactory;
    private final RequestTokenExtractor  tokenExtractor;

    @Inject
    public MachineLoginFilter(@Named("che.api") String apiEndpoint,
                              HttpJsonRequestFactory requestFactory,
                              RequestTokenExtractor tokenExtractor) {
        this.tokenServiceEndpoint = apiEndpoint + "/machine/token";
        this.requestFactory = requestFactory;
        this.tokenExtractor = tokenExtractor;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                                                                                                     ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest)request;
        final HttpSession session = httpRequest.getSession(false);
        if (session != null && session.getAttribute("principal") != null) {
            try {
                EnvironmentContext.getCurrent().setSubject((Subject)session.getAttribute("principal"));
                chain.doFilter(request, response);
                return;
            } finally {
                EnvironmentContext.reset();
            }
        }
        final String machineToken = tokenExtractor.getToken(httpRequest);
        if (isNullOrEmpty(machineToken)) {
            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                                      "Authentication on machine failed, token is missed");
            return;
        }
        try {
            final UserDto userDescriptor = requestFactory.fromUrl(tokenServiceEndpoint + "/user/" + machineToken)
                                                                .useGetMethod()
                                                                .setAuthorizationHeader(machineToken)
                                                                .request()
                                                                .asDto(UserDto.class);
            final Subject machineUser = new SubjectImpl(userDescriptor.getName(),
                                                        userDescriptor.getId(),
                                                        machineToken,
                                                        false);
            EnvironmentContext.getCurrent().setSubject(machineUser);
            final HttpSession httpSession = httpRequest.getSession(true);
            httpSession.setAttribute("principal", machineUser);
            chain.doFilter(request, response);
        } catch (NotFoundException nfEx) {
            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                                      "Authentication on machine failed, token " + machineToken + " is invalid");
        } catch (ApiException apiEx) {
            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, apiEx.getMessage());
        } finally {
            EnvironmentContext.reset();
        }
    }

    @Override
    public void destroy() {

    }
}
