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


import com.codenvy.api.permission.server.PermissionsModule;
import com.codenvy.api.permission.server.jpa.AbstractPermissionsDao;
import com.codenvy.api.permission.server.jpa.PermissionsJpaModule;
import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.Arrays;

import static org.testng.Assert.assertTrue;

/**
 * JPA-specific (non-TCK compliant) tests of {@link JpaWorkerDao}
 * @author Max Shaposhnik
 */
public class JpaWorkerDaoTest {

    private EntityManager manager;

    private JpaWorkerDao workerDao;

    private JpaWorkerDao.RemoveWorkersBeforeWorkspaceRemovedEventSubscriber removeWorkersBeforeWorkspaceRemovedEventSubscriber;

    private AbstractPermissionsDao.RemovePermissionsBeforeUserRemovedEventSubscriber removeWorkersBeforeUserRemovedEventSubscriber;

    WorkerImpl[] workers;

    UserImpl[] users;

    WorkspaceImpl[] workspaces;

    @BeforeClass
    public void setupEntities() throws Exception {
        workers = new WorkerImpl[]{new WorkerImpl("ws1", "user1", Arrays.asList("read", "use", "run")),
                                   new WorkerImpl("ws1", "user2", Arrays.asList("read", "use")),
                                   new WorkerImpl("ws2", "user1", Arrays.asList("read", "run")),
                                   new WorkerImpl("ws2", "user2", Arrays.asList("read", "use", "run", "configure"))};

        users = new UserImpl[]{new UserImpl("user1", "user1@com.com", "usr1"),
                               new UserImpl("user2", "user2@com.com", "usr2")};

        workspaces = new WorkspaceImpl[]{
                new WorkspaceImpl("ws1", users[0].getAccount(), new WorkspaceConfigImpl("", "", "cfg1", null, null, null)),
                new WorkspaceImpl("ws2", users[1].getAccount(), new WorkspaceConfigImpl("", "", "cfg2", null, null, null))};

        Injector injector =
                Guice.createInjector(new TestModule(), new WorkerJpaModule(), new PermissionsModule(), new PermissionsJpaModule());
        manager = injector.getInstance(EntityManager.class);
        workerDao = injector.getInstance(JpaWorkerDao.class);
        removeWorkersBeforeWorkspaceRemovedEventSubscriber = injector.getInstance(
                JpaWorkerDao.RemoveWorkersBeforeWorkspaceRemovedEventSubscriber.class);

        removeWorkersBeforeUserRemovedEventSubscriber =
                injector.getInstance(AbstractPermissionsDao.RemovePermissionsBeforeUserRemovedEventSubscriber.class);
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
    }

    @Test
    public void shouldRemoveWorkersWhenWorkspaceIsRemoved() throws Exception {
        BeforeWorkspaceRemovedEvent event = new BeforeWorkspaceRemovedEvent(workspaces[0]);
        removeWorkersBeforeWorkspaceRemovedEventSubscriber.onEvent(event);
        assertTrue(workerDao.getWorkers("ws1").isEmpty());
    }

    @Test
    public void shouldRemoveWorkersWhenUserIsRemoved() throws Exception {
        BeforeUserRemovedEvent event =  new BeforeUserRemovedEvent(new UserImpl("user1", "email@co.com", "user"));
        removeWorkersBeforeUserRemovedEventSubscriber.onEvent(event);
        assertTrue(workerDao.getWorkersByUser("user1").isEmpty());
    }


    private class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(new TypeLiteral<TckRepository<WorkerImpl>>() {}).toInstance(new JpaTckRepository<>(WorkerImpl.class));
            bind(new TypeLiteral<TckRepository<UserImpl>>() {}).toInstance(new JpaTckRepository<>(UserImpl.class));

            bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {}).toInstance(new JpaTckRepository<>(WorkspaceImpl.class));

            install(new JpaPersistModule("main"));
            bind(JpaInitializer.class).asEagerSingleton();
            bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();
            bind(org.eclipse.che.api.core.h2.jdbc.jpa.eclipselink.H2ExceptionHandler.class);
        }
    }
}
