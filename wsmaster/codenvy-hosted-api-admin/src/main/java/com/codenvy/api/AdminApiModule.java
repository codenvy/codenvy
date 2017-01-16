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
package com.codenvy.api;

import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.api.user.server.AdminUserCreator;
import com.codenvy.api.user.server.AdminUserService;
import com.codenvy.api.user.server.AdminUserServicePermissionsFilter;
import com.codenvy.api.user.server.UserProfileServicePermissionsFilter;
import com.codenvy.api.user.server.UserServicePermissionsFilter;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Initialize all components necessary for Admin User API.
 *
 * @author Anatoliy Bazko
 */
public class AdminApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AdminUserService.class);

        final Multibinder<String> binder = Multibinder.newSetBinder(binder(),
                                                                    String.class,
                                                                    Names.named(SystemDomain.SYSTEM_DOMAIN_ACTIONS));
        binder.addBinding().toInstance(UserServicePermissionsFilter.MANAGE_USERS_ACTION);

        bind(AdminUserServicePermissionsFilter.class);
        bind(UserProfileServicePermissionsFilter.class);
        bind(UserServicePermissionsFilter.class);
        bind(AdminUserCreator.class);
    }
}
