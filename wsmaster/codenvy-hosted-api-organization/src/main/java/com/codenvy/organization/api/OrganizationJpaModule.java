/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.organization.api;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.organization.spi.MemberDao;
import com.codenvy.organization.spi.OrganizationDao;
import com.codenvy.organization.spi.OrganizationDistributedResourcesDao;
import com.codenvy.organization.spi.impl.MemberImpl;
import com.codenvy.organization.spi.jpa.JpaMemberDao;
import com.codenvy.organization.spi.jpa.JpaOrganizationDao;
import com.codenvy.organization.spi.jpa.JpaOrganizationDistributedResourcesDao;
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
        bind(MemberDao.class).to(JpaMemberDao.class);

        bind(new TypeLiteral<AbstractPermissionsDomain<MemberImpl>>() {}).to(OrganizationDomain.class);

        Multibinder.newSetBinder(binder(), new TypeLiteral<PermissionsDao<? extends AbstractPermissions>>() {})
                   .addBinding().to(JpaMemberDao.class);

        bind(OrganizationDistributedResourcesDao.class).to(JpaOrganizationDistributedResourcesDao.class);
    }
}
