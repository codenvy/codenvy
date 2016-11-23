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
package com.codenvy.api.workspace.server.spi.jpa;

import com.codenvy.api.workspace.server.jpa.OnPremisesJpaWorkspaceModule;
import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.codenvy.api.workspace.server.spi.jpa.JpaWorkerDao.RemoveWorkersBeforeWorkspaceRemovedEventSubscriber;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao;
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
import java.util.stream.Stream;

import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link RemoveWorkersBeforeWorkspaceRemovedEventSubscriber}
 *
 * @author Sergii Leschenko
 */
public class RemoveWorkersBeforeWorkspaceRemovedEventSubscriberTest {
    private EntityManager   manager;
    private JpaWorkerDao    workerDao;
    private JpaWorkspaceDao workspaceDao;

    private RemoveWorkersBeforeWorkspaceRemovedEventSubscriber subscriber;

    private WorkspaceImpl workspace;
    private WorkerImpl[]  workers;
    private UserImpl[]    users;

    @BeforeClass
    public void setupEntities() throws Exception {
        users = new UserImpl[] {new UserImpl("user1", "user1@com.com", "usr1"),
                                new UserImpl("user2", "user2@com.com", "usr2")};

        workspace = new WorkspaceImpl("ws1", users[0].getAccount(), new WorkspaceConfigImpl("", "", "cfg1", null, null, null));

        workers = new WorkerImpl[] {new WorkerImpl("ws1", "user1", Arrays.asList("read", "use", "run")),
                                    new WorkerImpl("ws1", "user2", Arrays.asList("read", "use"))};

        Injector injector = Guice.createInjector(new TestModule(), new OnPremisesJpaWorkspaceModule());

        manager = injector.getInstance(EntityManager.class);
        workerDao = injector.getInstance(JpaWorkerDao.class);
        workspaceDao = injector.getInstance(JpaWorkspaceDao.class);
        subscriber = injector.getInstance(RemoveWorkersBeforeWorkspaceRemovedEventSubscriber.class);
        subscriber.subscribe();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        manager.getTransaction().begin();
        manager.persist(workspace);
        Stream.of(users).forEach(manager::persist);
        Stream.of(workers).forEach(manager::persist);
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
        subscriber.unsubscribe();
        manager.getEntityManagerFactory().close();
        H2TestHelper.shutdownDefault();
    }

    @Test
    public void shouldRemoveAllWorkersWhenWorkspaceIsRemoved() throws Exception {
        workspaceDao.remove(workspace.getId());

        assertEquals(workerDao.getWorkers(workspace.getId(), 1, 0).getTotalItemsCount(), 0);
    }

    @Test
    public void shouldRemoveAllWorkersWhenPageSizeEqualsToOne() throws Exception {
        subscriber.removeWorkers(workspace.getId(), 1);

        assertEquals(workerDao.getWorkers(workspace.getId(), 1, 0).getTotalItemsCount(), 0);
    }

    private class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            install(new JpaPersistModule("main"));
            bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema", "codenvy-schema"));
            bind(DBInitializer.class).asEagerSingleton();
        }
    }
}
