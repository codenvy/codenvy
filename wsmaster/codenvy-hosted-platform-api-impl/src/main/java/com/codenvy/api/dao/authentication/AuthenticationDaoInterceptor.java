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
