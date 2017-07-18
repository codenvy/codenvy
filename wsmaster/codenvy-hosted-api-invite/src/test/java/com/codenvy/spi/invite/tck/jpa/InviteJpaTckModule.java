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
package com.codenvy.spi.invite.tck.jpa;

import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.spi.invite.InviteDao;
import com.codenvy.api.invite.InviteImpl;
import com.codenvy.spi.invite.jpa.JpaInviteDao;
import com.google.inject.TypeLiteral;

import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConf2Impl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.db.H2DBTestServer;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.db.PersistTestModuleBuilder;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.h2.jpa.eclipselink.H2ExceptionHandler;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.h2.Driver;

/**
 * @author Sergii Leshchenko
 */
public class InviteJpaTckModule extends TckModule {
    @Override
    protected void configure() {
        H2DBTestServer server = H2DBTestServer.startDefault();
        install(new PersistTestModuleBuilder().setDriver(Driver.class)
                                              .runningOn(server)
                                              .addEntityClasses(OrganizationImpl.class,
                                                                AccountImpl.class,
                                                                WorkspaceImpl.class,
                                                                WorkspaceConfigImpl.class,
                                                                ProjectConfigImpl.class,
                                                                EnvironmentImpl.class,
                                                                ExtendedMachineImpl.class,
                                                                CommandImpl.class,
                                                                SourceStorageImpl.class,
                                                                ServerConf2Impl.class,
                                                                SnapshotImpl.class,
                                                                InviteImpl.class)
                                              .addEntityClass("org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl$Attribute")
                                              .setExceptionHandler(H2ExceptionHandler.class)
                                              .build());
        bind(DBInitializer.class).asEagerSingleton();
        bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema", "codenvy-schema"));
        bind(TckResourcesCleaner.class).toInstance(new H2JpaCleaner(server));

        bind(new TypeLiteral<TckRepository<InviteImpl>>() {}).toInstance(new JpaTckRepository<>(InviteImpl.class));
        bind(new TypeLiteral<TckRepository<OrganizationImpl>>() {}).toInstance(new JpaTckRepository<>(OrganizationImpl.class));
        bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {}).toInstance(new JpaTckRepository<>(WorkspaceImpl.class));

        bind(InviteDao.class).to(JpaInviteDao.class);
    }
}
