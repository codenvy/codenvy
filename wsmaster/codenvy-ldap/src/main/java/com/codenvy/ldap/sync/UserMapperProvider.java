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
package com.codenvy.ldap.sync;

import com.google.inject.Provider;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Container provider of UserMapper class
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class UserMapperProvider implements Provider<UserMapper> {


    private final UserMapper userMapper;

    /**
     * @param userIdAttr
     *         ldap attribute indicating user identifier, it must be unique, otherwise
     *         synchronization will fail on user which has the same identifier.
     *         e.g. 'uid'
     * @param userNameAttr
     *         ldap attribute indicating user name, it must be unique, otherwise
     *         synchronization will fail on user which has the same name.
     *         e.g. 'cn'
     * @param userEmailAttr
     *         ldap attribute indicating user email, it must be unique, otherwise
     *         synchronization will fail on user which has the same email
     *         e.g. 'mail'   */
    public UserMapperProvider(@Named("ldap.sync.user.attr.id") String userIdAttr,
                              @Named("ldap.sync.user.attr.name") String userNameAttr,
                              @Named("ldap.sync.user.attr.email") String userEmailAttr) {
        this.userMapper = new UserMapper(userIdAttr, userNameAttr, userEmailAttr);
    }

    @Override
    public UserMapper get() {
        return userMapper;
    }
}
