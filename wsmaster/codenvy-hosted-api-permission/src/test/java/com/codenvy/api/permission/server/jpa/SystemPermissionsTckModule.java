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
package com.codenvy.api.permission.server.jpa;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.api.permission.server.model.impl.SystemPermissionsImpl;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.permission.server.spi.tck.SystemPermissionsDaoTest;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.tck.JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;

import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
public class SystemPermissionsTckModule extends TckModule {

    @Override
    protected void configure() {
        //Creates empty multibinder to avoid error during container starting
        Multibinder.newSetBinder(binder(),
                                 String.class,
                                 Names.named(SystemDomain.SYSTEM_DOMAIN_ACTIONS));

        bind(new TypeLiteral<AbstractPermissionsDomain<SystemPermissionsImpl>>() {}).to(SystemPermissionsDaoTest.TestDomain.class);
        bind(new TypeLiteral<PermissionsDao<SystemPermissionsImpl>>() {}).to(JpaSystemPermissionsDao.class);


        bind(new TypeLiteral<TckRepository<SystemPermissionsImpl>>() {}).toInstance(new JpaTckRepository<>(SystemPermissionsImpl.class));
        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).toInstance(new JpaTckRepository<>(UserImpl.class));

        install(new JpaPersistModule("main"));
        bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema", "codenvy-schema"));
        bind(DBInitializer.class).asEagerSingleton();
        bind(TckResourcesCleaner.class).to(H2JpaCleaner.class);
    }
}
