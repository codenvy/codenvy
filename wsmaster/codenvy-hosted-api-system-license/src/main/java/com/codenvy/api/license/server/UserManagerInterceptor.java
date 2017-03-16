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
package com.codenvy.api.license.server;


import com.google.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;

import javax.inject.Singleton;


/**
 * Intercepts {@link org.eclipse.che.api.user.server.UserManager#create(User, boolean)} method and
 * {@link org.eclipse.che.api.user.server.UserManager#remove(String)}.
 *
 * The purpose of the interceptor is to notify {@link SystemLicenseManager} when number of users have been changed.
 * It allows avoiding unnecessary request to {@link UserManager#getTotalCount()}.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class UserManagerInterceptor implements MethodInterceptor {
    @Inject
    private SystemLicenseManager systemLicenseManager;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object proceed = invocation.proceed();

        String methodName = invocation.getMethod().getName();
        switch (methodName) {
            case "create":
                systemLicenseManager.onUsersNumberChanged(1);
                break;
            case "remove":
                systemLicenseManager.onUsersNumberChanged(-1);
                break;
        }

        return proceed;
    }
}
