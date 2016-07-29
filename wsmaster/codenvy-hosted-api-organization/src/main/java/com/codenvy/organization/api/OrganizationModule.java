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

import com.codenvy.api.permission.server.dao.PermissionsStorage;
import com.codenvy.organization.api.permissions.OrganizationCreatorPermissionsProvider;
import com.codenvy.organization.api.permissions.OrganizationPermissionStorage;
import com.codenvy.organization.api.permissions.OrganizationPermissionsFilter;
import com.codenvy.organization.spi.MemberDao;
import com.codenvy.organization.spi.OrganizationDao;
import com.codenvy.organization.spi.jpa.JpaMemberDao;
import com.codenvy.organization.spi.jpa.JpaOrganizationDao;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Sergii Leschenko
 */
public class OrganizationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(OrganizationDao.class).to(JpaOrganizationDao.class);
        bind(MemberDao.class).to(JpaMemberDao.class);

        bind(OrganizationService.class);
        bind(OrganizationPermissionsFilter.class);

        bind(JpaMemberDao.RemoveMembersBeforeOrganizationRemovedEventSubscriber.class).asEagerSingleton();
        bind(JpaMemberDao.RemoveMembersBeforeUserRemovedEventSubscriber.class).asEagerSingleton();

        bind(OrganizationCreatorPermissionsProvider.class).asEagerSingleton();
        Multibinder<PermissionsStorage> storages = Multibinder.newSetBinder(binder(),
                                                                            PermissionsStorage.class);
        storages.addBinding().to(OrganizationPermissionStorage.class);
    }
}
