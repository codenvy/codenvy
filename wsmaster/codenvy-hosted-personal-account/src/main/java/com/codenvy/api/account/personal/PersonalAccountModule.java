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
package com.codenvy.api.account.personal;

import com.codenvy.api.permission.server.account.AccountPermissionsChecker;
import com.codenvy.resource.api.free.DefaultResourcesProvider;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.inject.DynaModule;

/**
 * @author Sergii Leschenko
 */
@DynaModule
public class PersonalAccountModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(UserManager.class).to(OnpremisesUserManager.class);

        Multibinder.newSetBinder(binder(), DefaultResourcesProvider.class)
                   .addBinding().to(DefaultUserResourcesProvider.class);

        Multibinder.newSetBinder(binder(), AccountPermissionsChecker.class)
                   .addBinding().to(PersonalAccountPermissionsChecker.class);
    }
}
