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
import com.codenvy.api.permission.server.dao.CommonDomains;
import com.codenvy.api.permission.server.model.impl.PermissionsImpl;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.permission.server.spi.tck.PermissionsDaoTest;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;

/**
 * @author Max Shaposhnik
 *
 */
public class PermissionsTckModule extends TckModule {
    @Override
    protected void configure() {

        final Multibinder<AbstractPermissionsDomain> permissionsDomainMultibinder = Multibinder.newSetBinder(binder(),
                                                                                                             AbstractPermissionsDomain.class,
                                                                                                             CommonDomains.class);

        permissionsDomainMultibinder.addBinding().to(PermissionsDaoTest.TestDomain.class);
        bind(PermissionsDao.class).to(JpaCommonPermissionsDao.class);
        bind(new TypeLiteral<TckRepository<PermissionsImpl>>() {}).toInstance(new JpaTckRepository<>(PermissionsImpl.class));

        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).toInstance(new JpaTckRepository<>(UserImpl.class));
        bind(new TypeLiteral<TckRepository<StackImpl>>() {}).toInstance(new JpaTckRepository<>(StackImpl.class));
        bind(new TypeLiteral<TckRepository<RecipeImpl>>() {}).toInstance(new JpaTckRepository<>(RecipeImpl.class));


        install(new JpaPersistModule("main"));
        bind(JpaInitializer.class).asEagerSingleton();
        bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();
        bind(org.eclipse.che.api.core.h2.jdbc.jpa.eclipselink.H2ExceptionHandler.class);
    }
}
