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
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

/**
 * @author Max Shaposhnik
 *
 */
public class PermissionsJpaModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(new TypeLiteral<AbstractPermissionsDomain<SystemDomain.SystemPermissionsImpl>>() {}).to(SystemDomain.class);
        bind(new TypeLiteral<PermissionsDao<SystemDomain.SystemPermissionsImpl>>() {}).to(JpaSystemPermissionsDao.class);
        bind(AbstractPermissionsDao.RemovePermissionsBeforeUserRemovedEventSubscriber.class).asEagerSingleton();
    }
}
