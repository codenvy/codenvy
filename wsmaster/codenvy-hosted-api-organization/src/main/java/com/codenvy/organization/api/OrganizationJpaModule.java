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
package com.codenvy.organization.api;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.organization.api.permissions.RemoveOrganizationOnLastUserRemovedEventSubscriber;
import com.codenvy.organization.spi.MemberDao;
import com.codenvy.organization.spi.OrganizationDao;
import com.codenvy.organization.spi.impl.MemberImpl;
import com.codenvy.organization.spi.jpa.JpaMemberDao;
import com.codenvy.organization.spi.jpa.JpaOrganizationDao;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Sergii Leschenko
 */
public class OrganizationJpaModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(OrganizationDao.class).to(JpaOrganizationDao.class);
        bind(JpaOrganizationDao.RemoveSuborganizationsBeforeParentOrganizationRemovedEventSubscriber.class).asEagerSingleton();
        bind(new TypeLiteral<AbstractPermissionsDomain<MemberImpl>>() {}).to(OrganizationDomain.class);
        bind(MemberDao.class).to(JpaMemberDao.class);
        bind(JpaMemberDao.RemoveMembersBeforeOrganizationRemovedEventSubscriber.class).asEagerSingleton();
        bind(RemoveOrganizationOnLastUserRemovedEventSubscriber.class).asEagerSingleton();

        bind(new TypeLiteral<AbstractPermissionsDomain<MemberImpl>>() {}).to(OrganizationDomain.class);

        Multibinder<PermissionsDao<? extends AbstractPermissions>> storages =
                Multibinder.newSetBinder(binder(), new TypeLiteral<PermissionsDao<? extends AbstractPermissions>>() {});
        storages.addBinding().to(JpaMemberDao.class);
    }
}
