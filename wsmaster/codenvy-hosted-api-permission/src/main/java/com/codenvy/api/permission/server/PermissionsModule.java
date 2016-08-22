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
package com.codenvy.api.permission.server;

import com.codenvy.api.permission.server.filter.GetPermissionsFilter;
import com.codenvy.api.permission.server.filter.RemovePermissionsFilter;
import com.codenvy.api.permission.server.filter.SetPermissionsFilter;
import com.codenvy.api.permission.server.jpa.AbstractPermissionsDao;
import com.codenvy.api.permission.server.jpa.JpaSystemPermissionsDao;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * @author Sergii Leschenko
 */
public class PermissionsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PermissionsService.class);
        bind(SetPermissionsFilter.class);
        bind(RemovePermissionsFilter.class);
        bind(GetPermissionsFilter.class);

        //Creates empty multibinder to avoid error during container starting
        Multibinder.newSetBinder(binder(),
                                 String.class,
                                 Names.named(SystemDomain.SYSTEM_DOMAIN_ACTIONS));


        Multibinder<AbstractPermissionsDao> storages = Multibinder.newSetBinder(binder(),
                                                                        AbstractPermissionsDao.class);
        storages.addBinding().to(JpaSystemPermissionsDao.class);
    }
}
