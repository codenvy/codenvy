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
package com.codenvy.organization.spi.tck.jpa;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.organization.spi.MemberDao;
import com.codenvy.organization.spi.OrganizationDao;
import com.codenvy.organization.spi.OrganizationDistributedResourcesDao;
import com.codenvy.organization.spi.impl.MemberImpl;
import com.codenvy.organization.spi.impl.OrganizationDistributedResourcesImpl;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.organization.spi.jpa.JpaMemberDao;
import com.codenvy.organization.spi.jpa.JpaOrganizationDao;
import com.codenvy.organization.spi.jpa.JpaOrganizationDistributedResourcesDao;
import com.codenvy.organization.spi.jpa.JpaOrganizationImplTckRepository;
import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;

import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;

/**
 * @author Sergii Leschenko
 */
public class OrganizationJpaTckModule extends TckModule {

    @Override
    protected void configure() {
        install(new JpaPersistModule("main"));
        bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema", "codenvy-schema"));
        bind(DBInitializer.class).asEagerSingleton();
        bind(TckResourcesCleaner.class).to(H2JpaCleaner.class);

        bind(new TypeLiteral<AbstractPermissionsDomain<MemberImpl>>() {}).to(OrganizationDomain.class);

        bind(new TypeLiteral<TckRepository<OrganizationImpl>>() {}).to(JpaOrganizationImplTckRepository.class);
        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).toInstance(new JpaTckRepository<>(UserImpl.class));
        bind(new TypeLiteral<TckRepository<MemberImpl>>() {}).toInstance(new JpaTckRepository<>(MemberImpl.class));
        bind(new TypeLiteral<TckRepository<OrganizationDistributedResourcesImpl>>() {})
                .toInstance(new JpaTckRepository<>(OrganizationDistributedResourcesImpl.class));

        bind(OrganizationDao.class).to(JpaOrganizationDao.class);
        bind(MemberDao.class).to(JpaMemberDao.class);

        bind(OrganizationDistributedResourcesDao.class).to(JpaOrganizationDistributedResourcesDao.class);
    }
}
