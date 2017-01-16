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
package com.codenvy.api.machine.server.jpa;

import com.codenvy.api.machine.server.recipe.RecipePermissionsImpl;
import com.codenvy.api.machine.server.spi.tck.RecipePermissionsDaoTest;
import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.machine.server.jpa.JpaRecipeDao;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
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
 * @author Max Shaposhnik
 */
public class RecipePermissionsTckModule extends TckModule {
    @Override
    protected void configure() {
        install(new JpaPersistModule("main"));
        bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema", "codenvy-schema"));
        bind(DBInitializer.class).asEagerSingleton();
        bind(TckResourcesCleaner.class).to(H2JpaCleaner.class);

        bind(new TypeLiteral<AbstractPermissionsDomain<RecipePermissionsImpl>>() {}).to(RecipePermissionsDaoTest.TestDomain.class);
        bind(new TypeLiteral<PermissionsDao<RecipePermissionsImpl>>() {}).to(JpaRecipePermissionsDao.class);
        bind(new TypeLiteral<TckRepository<RecipePermissionsImpl>>() {}).toInstance(new JpaTckRepository<>(RecipePermissionsImpl.class));
        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).toInstance(new JpaTckRepository<>(UserImpl.class));
        bind(new TypeLiteral<TckRepository<RecipeImpl>>() {}).toInstance(new JpaTckRepository<>(RecipeImpl.class));

        bind(RecipeDao.class).to(JpaRecipeDao.class);
    }
}
