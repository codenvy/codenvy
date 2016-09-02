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
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.codenvy.api.workspace.server.recipe.RecipeDomain;
import com.codenvy.api.workspace.server.recipe.RecipePermissionsImpl;
import com.codenvy.api.workspace.server.spi.WorkerDao;
import com.codenvy.api.workspace.server.stack.StackDomain;
import com.codenvy.api.workspace.server.stack.StackPermissionsImpl;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Max Shaposhnik
 */
public class PermissionsJpaModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(WorkerDao.class).to(JpaWorkerDao.class);
        bind(JpaWorkerDao.RemoveWorkersBeforeWorkspaceRemovedEventSubscriber.class).asEagerSingleton();
        bind(JpaWorkerDao.RemoveSystemPermissionsBeforeUserRemovedEventSubscriber.class).asEagerSingleton();
        bind(RemovePermissionsOnLastSetPermissionsUserRemovedEventSubscriber.class).asEagerSingleton();
        bind(JpaStackPermissionsDao.RemovePermissionsBeforeStackRemovedEventSubscriber.class).asEagerSingleton();
        bind(JpaRecipePermissionsDao.RemovePermissionsBeforeRecipeRemovedEventSubscriber.class).asEagerSingleton();

        bind(new TypeLiteral<AbstractPermissionsDomain<RecipePermissionsImpl>>() {
        }).to(RecipeDomain.class);
        bind(new TypeLiteral<AbstractPermissionsDomain<StackPermissionsImpl>>() {}).to(StackDomain.class);
        bind(new TypeLiteral<AbstractPermissionsDomain<WorkerImpl>>() {}).to(WorkspaceDomain.class);


        Multibinder<PermissionsDao<? extends AbstractPermissions>> storages =
                Multibinder.newSetBinder(binder(),new TypeLiteral<PermissionsDao<? extends AbstractPermissions>>() {});
        storages.addBinding().to(JpaWorkerDao.class);
        storages.addBinding().to(JpaRecipePermissionsDao.class);
        storages.addBinding().to(JpaStackPermissionsDao.class);
    }
}
