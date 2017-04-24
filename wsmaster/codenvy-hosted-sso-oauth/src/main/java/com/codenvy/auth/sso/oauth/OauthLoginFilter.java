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
package com.codenvy.auth.sso.oauth;

import com.codenvy.api.dao.authentication.PassportValidator;

import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Performs country validation before finishing OAuth login
 *
 * @author Max Shaposhnik
 */
@Singleton
public class OauthLoginFilter implements Filter {

    @Inject
    private PassportValidator passportValidator;

    @Inject
    private UserManager userManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            Optional<User> userOptional = getUserByEmail(httpRequest.getParameter("email"));
            if (userOptional.isPresent()) {
                passportValidator.validate(userOptional.get().getId());
            }
        } catch (AuthenticationException e) {
            ((HttpServletResponse)response).sendError(e.getResponseStatus(), e.getLocalizedMessage());
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }


    private Optional<User> getUserByEmail(String email) throws IOException {
        if (email == null) {
            return Optional.empty();
        }
        try {
            final User user = userManager.getByEmail(email);
            return Optional.of(user);
        } catch (NotFoundException e) {
            return Optional.empty();
        } catch (ServerException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

}
