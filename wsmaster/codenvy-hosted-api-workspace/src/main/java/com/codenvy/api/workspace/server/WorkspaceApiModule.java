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

import com.codenvy.api.permission.server.jpa.AbstractPermissionsDao;
import com.codenvy.api.workspace.server.filters.RecipeScriptDownloadPermissionFilter;
import com.codenvy.api.workspace.server.filters.WorkspacePermissionsFilter;
import com.codenvy.api.workspace.server.jpa.JpaRecipePermissionsDao;
import com.codenvy.api.workspace.server.jpa.JpaStackPermissionsDao;
import com.codenvy.api.workspace.server.jpa.JpaWorkerDao;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Sergii Leschenko
 */
public class WorkspaceApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WorkspacePermissionsFilter.class);
//        bind(RecipePermissionsFilter.class);
//        bind(StackPermissionsFilter.class);
//        bind(AclSetPermissionsFilter.class);
        bind(RecipeScriptDownloadPermissionFilter.class);

        bind(WorkspaceCreatorPermissionsProvider.class).asEagerSingleton();

        Multibinder<AbstractPermissionsDao> storages = Multibinder.newSetBinder(binder(),
                                                                                AbstractPermissionsDao.class);
        storages.addBinding().to(JpaWorkerDao.class);
        storages.addBinding().to(JpaRecipePermissionsDao.class);
        storages.addBinding().to(JpaStackPermissionsDao.class);
    }
}
