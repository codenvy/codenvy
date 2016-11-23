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

import com.codenvy.api.machine.server.jpa.OnPremisesJpaMachineModule;
import com.codenvy.api.permission.server.PermissionsModule;
import com.codenvy.api.permission.server.jpa.SystemPermissionsJpaModule;
import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.codenvy.api.workspace.server.spi.jpa.OnPremisesJpaStackDao;
import com.codenvy.api.workspace.server.spi.jpa.OnPremisesJpaWorkspaceDao;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.db.H2TestHelper;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Max Shaposhnik
 */
public class OnPremisesJpaWorkspaceDaoTest {
    private EntityManager             manager;
    private OnPremisesJpaWorkspaceDao dao;

    private WorkerImpl[]    workers;
    private UserImpl[]      users;
    private WorkspaceImpl[] workspaces;

    @BeforeClass
    public void setupEntities() throws Exception {
        workers = new WorkerImpl[] {new WorkerImpl("ws1", "user1", Arrays.asList("read", "use", "search")),
                                    new WorkerImpl("ws2", "user1", Arrays.asList("read", "search")),
                                    new WorkerImpl("ws3", "user1", Arrays.asList("none", "run")),
                                    new WorkerImpl("ws1", "user2", Arrays.asList("read", "use"))};

        users = new UserImpl[] {new UserImpl("user1", "user1@com.com", "usr1"),
                                new UserImpl("user2", "user2@com.com", "usr2")};

        workspaces = new WorkspaceImpl[] {
                new WorkspaceImpl("ws1", users[0].getAccount(), new WorkspaceConfigImpl("wrksp1", "", "cfg1", null, null, null)),
                new WorkspaceImpl("ws2", users[0].getAccount(), new WorkspaceConfigImpl("wrksp2", "", "cfg2", null, null, null)),
                new WorkspaceImpl("ws3", users[0].getAccount(), new WorkspaceConfigImpl("wrksp3", "", "cfg3", null, null, null))};
        Injector injector =
                Guice.createInjector(new TestModule(), new OnPremisesJpaMachineModule(), new PermissionsModule(),
                                     new SystemPermissionsJpaModule());
        manager = injector.getInstance(EntityManager.class);
        dao = injector.getInstance(OnPremisesJpaWorkspaceDao.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        manager.getTransaction().begin();
        for (UserImpl user : users) {
            manager.persist(user);
        }

        for (WorkspaceImpl ws : workspaces) {
            manager.persist(ws);
        }

        for (WorkerImpl worker : workers) {
            manager.persist(worker);
        }
        manager.getTransaction().commit();
        manager.clear();
    }

    @AfterMethod
    public void cleanup() {
        manager.getTransaction().begin();

        manager.createQuery("SELECT e FROM Worker e", WorkerImpl.class)
               .getResultList()
               .forEach(manager::remove);

        manager.createQuery("SELECT w FROM Workspace w", WorkspaceImpl.class)
               .getResultList()
               .forEach(manager::remove);

        manager.createQuery("SELECT u FROM Usr u", UserImpl.class)
               .getResultList()
               .forEach(manager::remove);
        manager.getTransaction().commit();
    }

    @AfterClass
    public void shutdown() throws Exception {
        manager.getEntityManagerFactory().close();
        H2TestHelper.shutdownDefault();
    }

    @Test
    public void shouldFindStackByPermissions() throws Exception {
        List<WorkspaceImpl> results = dao.getWorkspaces(users[0].getId());
        assertEquals(results.size(), 2);
        assertTrue(results.contains(workspaces[0]));
        assertTrue(results.contains(workspaces[1]));
    }

    private class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(JpaStackDao.class).to(OnPremisesJpaStackDao.class);
            install(new JpaPersistModule("main"));
            bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema", "codenvy-schema"));
            bind(DBInitializer.class).asEagerSingleton();
        }
    }
}
