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
package com.codenvy.api.dao.authentication;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.auth.shared.dto.Token;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

/**
 * Intercepts calls to {@link AuthenticationDaoImpl} to perform passport validation.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class AuthenticationDaoInterceptor implements MethodInterceptor {

    @Inject
    private TicketManager ticketManager;

    @Inject
    private PassportValidator passportValidator;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if (result instanceof Response && ((Response)result).getStatus() == Response.Status.OK.getStatusCode()) {
            final Token token = (Token)((Response)result).getEntity();
            final AccessTicket ticket = ticketManager.getAccessTicket(token.getValue());
            if (ticket != null) {
                passportValidator.validate(ticket.getUserId());
            }
        }
        return result;
    }
}
