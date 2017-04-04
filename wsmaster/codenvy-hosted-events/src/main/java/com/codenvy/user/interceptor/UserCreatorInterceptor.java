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

import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.user.CreationNotificationSender;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Intercepts {@link UserCreator#createUser(String, String, String, String)} method.
 *
 * <p>The purpose of the interceptor is to send "welcome to codenvy" email to user after its creation.
 *
 * @author Anatoliy Bazko
 * @author Sergii Leschenko
 */
@Singleton
public class UserCreatorInterceptor implements MethodInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(UserCreatorInterceptor.class);

    @Inject
    private UserManager userManager;

    @Inject
    private CreationNotificationSender notificationSender;

    //Do not remove ApiException. It used to tell dependency plugin that api-core is need not only for tests.
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable, ApiException {
        //UserCreator should not create user if he already exists
        final String email = (String)invocation.getArguments()[0];
        try {
            userManager.getByEmail(email);

            //user is already registered
            return invocation.proceed();
        } catch (NotFoundException e) {
            //user was not found and it will be created
        }

        final Object proceed = invocation.proceed();
        try {
            final User createdUser = (User)proceed;
            notificationSender.sendNotification(createdUser.getName(),
                                                createdUser.getEmail(),
                                                false);
        } catch (Exception e) {
            LOG.warn("Unable to send creation notification email", e);
        }

        return proceed;
    }
}
