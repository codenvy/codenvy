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
package com.codenvy.api.workspace.server;

import com.codenvy.api.permission.server.dao.PermissionsStorage;
import com.codenvy.api.workspace.server.filters.MachinePermissionsFilter;
import com.codenvy.api.workspace.server.filters.WorkspacePermissionsFilter;
import com.codenvy.api.workspace.server.recipe.RecipeCreatorPermissionsProvider;
import com.codenvy.api.workspace.server.recipe.RecipePermissionStorage;
import com.codenvy.api.workspace.server.recipe.RecipePermissionsFilter;
import com.codenvy.api.workspace.server.stack.StackCreatorPermissionsProvider;
import com.codenvy.api.workspace.server.stack.StackPermissionStorage;
import com.codenvy.api.workspace.server.stack.StackPermissionsFilter;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.machine.server.dao.RecipeDao;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.stack.StackService;
import org.eclipse.che.inject.Matchers;

import static com.google.inject.matcher.Matchers.subclassesOf;

/**
 * @author Sergii Leschenko
 */
public class WorkspaceApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WorkspacePermissionsFilter.class);
        bind(RecipePermissionsFilter.class);
        bind(MachinePermissionsFilter.class);
        bind(StackPermissionsFilter.class);

        bind(WorkspaceCreatorPermissionsProvider.class).asEagerSingleton();
        bind(WorkspacePermissionsRemover.class).asEagerSingleton();

        Multibinder<PermissionsStorage> storages = Multibinder.newSetBinder(binder(),
                                                                            PermissionsStorage.class);
        storages.addBinding().to(WorkspacePermissionStorage.class);
        storages.addBinding().to(StackPermissionStorage.class);
        storages.addBinding().to(RecipePermissionStorage.class);

        StackCreatorPermissionsProvider stackCreatorPermissionsProvider = new StackCreatorPermissionsProvider();
        requestInjection(stackCreatorPermissionsProvider);
        bindInterceptor(subclassesOf(StackDao.class), Matchers.names("create"), stackCreatorPermissionsProvider);

        RecipeCreatorPermissionsProvider recipeCreatorPermissionsProvider = new RecipeCreatorPermissionsProvider();
        requestInjection(recipeCreatorPermissionsProvider);
        bindInterceptor(subclassesOf(RecipeDao.class), Matchers.names("create"), recipeCreatorPermissionsProvider);
    }
}
