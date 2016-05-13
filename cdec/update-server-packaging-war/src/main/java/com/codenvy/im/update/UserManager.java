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
package com.codenvy.im.update;

import com.google.inject.Singleton;

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class UserManager {

    public static final String ANONYMOUS_USER_NAME = "Anonymous";

    public Subject getCurrentUser() {
        return EnvironmentContext.getCurrent().getSubject();
    }

    public boolean isAnonymous() {
        return getCurrentUser() == null
               || ANONYMOUS_USER_NAME.equals(getCurrentUser().getUserName());
    }
}
