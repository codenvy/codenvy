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
package com.codenvy.api.workspace.server.jpa;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.workspace.server.recipe.RecipePermissionsImpl;
import com.codenvy.api.workspace.server.spi.tck.RecipePermissionsDaoTest;
import com.google.inject.TypeLiteral;

import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;

/**
 * @author Max Shaposhnik
 *
 */
public class RecipePermissionsTckModule extends TckModule {
    @Override
    protected void configure() {

        bind(new TypeLiteral<AbstractPermissionsDomain<RecipePermissionsImpl>>() {}).to(RecipePermissionsDaoTest.TestDomain.class);

        bind(new TypeLiteral<PermissionsDao<RecipePermissionsImpl>>() {}).to(JpaRecipePermissionsDao.class);
        bind(new TypeLiteral<TckRepository<RecipePermissionsImpl>>() {}).toInstance(new JpaTckRepository<>(RecipePermissionsImpl.class));

        bind(new TypeLiteral<TckRepository<RecipeImpl>>() {}).toInstance(new JpaTckRepository<>(RecipeImpl.class));

        bind(JpaInitializer.class).asEagerSingleton();
        bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();
        bind(org.eclipse.che.api.core.h2.jdbc.jpa.eclipselink.H2ExceptionHandler.class);
    }
}
