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
package com.codenvy.api.permission.server.jpa;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.server.model.impl.SystemPermissionsImpl;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Max Shaposhnik
 */
public class PermissionsJpaModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(new TypeLiteral<AbstractPermissionsDomain<SystemPermissionsImpl>>() {}).to(SystemDomain.class);

        bind(AbstractPermissionsDao.RemovePermissionsBeforeUserRemovedEventSubscriber.class).asEagerSingleton();

        Multibinder<PermissionsDao<? extends AbstractPermissions>> storages = Multibinder.newSetBinder(binder(),
                                                                        new TypeLiteral<PermissionsDao<? extends AbstractPermissions>>() {});
        storages.addBinding().to(JpaSystemPermissionsDao.class);
    }
}
