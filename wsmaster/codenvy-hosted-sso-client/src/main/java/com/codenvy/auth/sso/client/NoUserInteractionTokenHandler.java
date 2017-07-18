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
package com.codenvy.auth.sso.client;

import org.eclipse.che.api.core.UnauthorizedException;
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
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(DtoFactory.getInstance()
                                   .toJson(new UnauthorizedException("User not authorized to call this method.").getServiceError()));
        }
    }
}
