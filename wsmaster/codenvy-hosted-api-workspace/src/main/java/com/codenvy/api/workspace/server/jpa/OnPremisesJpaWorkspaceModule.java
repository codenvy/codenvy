/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
