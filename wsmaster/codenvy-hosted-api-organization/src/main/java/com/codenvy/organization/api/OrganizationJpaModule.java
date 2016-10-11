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
