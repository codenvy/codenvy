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
package com.codenvy.auth.sso.client;

import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * InvalidTokenHandler that not supposed any user interaction action.
 *
 * @author Sergii Kabashniuk
 */
public class NoUserInteractionTokenHandler extends DefaultTokenHandler {

    @Inject
    public NoUserInteractionTokenHandler(RequestWrapper requestWrapper) {
        super(requestWrapper);
    }

    @Override
    public void handleBadToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String token)
            throws IOException,
                   ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(DtoFactory.getInstance()
                                   .toJson(new UnauthorizedException("Provided token " + token + " is invalid").getServiceError()));
        }
    }

    @Override
    public void handleMissingToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        //go with anonymous
        EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
        environmentContext.setSubject(Subject.ANONYMOUS);
        chain.doFilter(requestWrapper.wrapRequest(request.getSession(), request, Subject.ANONYMOUS), response);
    }
}
