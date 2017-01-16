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
package com.codenvy.api.workspace.server.jpa;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.jpa.listener.RemovePermissionsOnLastUserRemovedEventSubscriber;
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.api.workspace.server.jpa.listener.RemoveStackOnLastUserRemovedEventSubscriber;
import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.codenvy.api.workspace.server.spi.WorkerDao;
import com.codenvy.api.workspace.server.spi.jpa.JpaStackPermissionsDao;
import com.codenvy.api.workspace.server.spi.jpa.JpaWorkerDao;
import com.codenvy.api.workspace.server.spi.jpa.OnPremisesJpaStackDao;
import com.codenvy.api.workspace.server.spi.jpa.OnPremisesJpaWorkspaceDao;
import com.codenvy.api.workspace.server.stack.StackDomain;
import com.codenvy.api.workspace.server.stack.StackPermissionsImpl;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao;

/**
 * @author Max Shaposhnik
 */
public class OnPremisesJpaWorkspaceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WorkerDao.class).to(JpaWorkerDao.class);
        bind(JpaWorkspaceDao.class).to(OnPremisesJpaWorkspaceDao.class);
        bind(JpaStackDao.class).to(OnPremisesJpaStackDao.class);

        bind(JpaWorkerDao.RemoveWorkersBeforeWorkspaceRemovedEventSubscriber.class).asEagerSingleton();
        bind(JpaWorkerDao.RemoveWorkersBeforeUserRemovedEventSubscriber.class).asEagerSingleton();

        bind(new TypeLiteral<RemovePermissionsOnLastUserRemovedEventSubscriber<JpaStackPermissionsDao>>() {
        }).to(RemoveStackOnLastUserRemovedEventSubscriber.class);
        bind(JpaStackPermissionsDao.RemovePermissionsBeforeStackRemovedEventSubscriber.class).asEagerSingleton();

        bind(new TypeLiteral<AbstractPermissionsDomain<StackPermissionsImpl>>() {}).to(StackDomain.class);
        bind(new TypeLiteral<AbstractPermissionsDomain<WorkerImpl>>() {}).to(WorkspaceDomain.class);

        Multibinder<PermissionsDao<? extends AbstractPermissions>> daos =
                Multibinder.newSetBinder(binder(), new TypeLiteral<PermissionsDao<? extends AbstractPermissions>>() {});
        daos.addBinding().to(JpaWorkerDao.class);
        daos.addBinding().to(JpaStackPermissionsDao.class);
    }
}
