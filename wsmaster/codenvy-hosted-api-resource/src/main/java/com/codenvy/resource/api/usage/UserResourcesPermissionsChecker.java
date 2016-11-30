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
package com.codenvy.resource.api.usage;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Singleton;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class UserResourcesPermissionsChecker implements ResourcesPermissionsChecker {
    @Override
    public void checkResourcesVisibility(String accountId) throws ForbiddenException {
        if (EnvironmentContext.getCurrent().getSubject().getUserId().equals(accountId)) {
            //user should be able to see resources of his personal account
            return;
        }

        throw new ForbiddenException("User is not authorized to see resources information of requested account.");
    }

    @Override
    public String getAccountType() {
        return UserImpl.PERSONAL_ACCOUNT;
    }
}
