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
