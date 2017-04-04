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
package com.codenvy.user.interceptor;

import com.codenvy.user.CreationNotificationSender;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

/**
 * Intercepts {@link UserService#create(UserDto, String, Boolean)} method.
 *
 * <p>The purpose of the interceptor is to send "welcome to codenvy" email to user after its creation.
 *
 * @author Anatoliy Bazko
 * @author Sergii Leschenko
 */
@Singleton
public class CreateUserInterceptor implements MethodInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(CreateUserInterceptor.class);

    @Inject
    private CreationNotificationSender notificationSender;

    //Do not remove ApiException. It used to tell dependency plugin that api-core is need not only for tests.
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable, ApiException {
        Object proceed = invocation.proceed();

        String userName;
        String userEmail;
        try {
            final Response response = (Response)proceed;
            final UserDto createdUser = (UserDto)response.getEntity();
            userName = createdUser.getName();
            userEmail = createdUser.getEmail();
            final String token = (String)invocation.getArguments()[1];
            notificationSender.sendNotification(userName, userEmail, token == null);
        } catch (Exception e) {
            LOG.warn("Unable to send creation notification email", e);
        }

        return proceed;
    }
}
