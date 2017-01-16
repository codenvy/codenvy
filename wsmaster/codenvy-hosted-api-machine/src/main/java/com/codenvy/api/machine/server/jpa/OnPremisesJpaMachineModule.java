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

import com.codenvy.api.machine.server.jpa.listener.RemoveRecipeOnLastUserRemovedEventSubscriber;
import com.codenvy.api.machine.server.recipe.RecipeDomain;
import com.codenvy.api.machine.server.recipe.RecipePermissionsImpl;
import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.machine.server.jpa.JpaRecipeDao;

/**
 * @author Anton Korneta
 */
public class OnPremisesJpaMachineModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(JpaRecipeDao.class).to(OnPremisesJpaRecipeDao.class);
        bind(new TypeLiteral<AbstractPermissionsDomain<RecipePermissionsImpl>>() {}).to(RecipeDomain.class);

        final Multibinder<PermissionsDao<? extends AbstractPermissions>> daos =
                Multibinder.newSetBinder(binder(), new TypeLiteral<PermissionsDao<? extends AbstractPermissions>>() {});
        daos.addBinding().to(JpaRecipePermissionsDao.class);

        bind(new TypeLiteral<AbstractPermissionsDomain<RecipePermissionsImpl>>() {}).to(RecipeDomain.class);
        bind(JpaRecipePermissionsDao.RemovePermissionsBeforeRecipeRemovedEventSubscriber.class).asEagerSingleton();
        bind(RemoveRecipeOnLastUserRemovedEventSubscriber.class).asEagerSingleton();
    }
}
