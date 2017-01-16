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
package com.codenvy.api.workspace.server.spi.jpa;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.codenvy.api.workspace.server.spi.WorkerDao;
import com.codenvy.api.workspace.server.spi.tck.StackPermissionsDaoTest;
import com.codenvy.api.workspace.server.spi.tck.WorkerDaoTest;
import com.codenvy.api.workspace.server.stack.StackPermissionsImpl;
import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;

import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;

/**
 * @author Yevhenii Voevodin
 */
public class JpaTckModule extends TckModule {

    @Override
    protected void configure() {
        bind(new TypeLiteral<AbstractPermissionsDomain<StackPermissionsImpl>>() {}).to(StackPermissionsDaoTest.TestDomain.class);
        bind(new TypeLiteral<PermissionsDao<StackPermissionsImpl>>() {}).to(JpaStackPermissionsDao.class);
        bind(new TypeLiteral<TckRepository<StackPermissionsImpl>>() {}).toInstance(new JpaTckRepository<>(StackPermissionsImpl.class));
        bind(new TypeLiteral<TckRepository<StackImpl>>() {}).toInstance(new JpaTckRepository<>(StackImpl.class));

        bind(new TypeLiteral<AbstractPermissionsDomain<WorkerImpl>>() {}).to(WorkerDaoTest.TestDomain.class);

        bind(WorkerDao.class).to(JpaWorkerDao.class);
        bind(new TypeLiteral<TckRepository<WorkerImpl>>() {}).toInstance(new JpaTckRepository<>(WorkerImpl.class));
        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).toInstance(new JpaTckRepository<>(UserImpl.class));

        bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {}).toInstance(new JpaTckRepository<>(WorkspaceImpl.class));


        install(new JpaPersistModule("main"));
        bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema", "codenvy-schema"));
        bind(DBInitializer.class).asEagerSingleton();
        bind(TckResourcesCleaner.class).to(H2JpaCleaner.class);
    }
}
