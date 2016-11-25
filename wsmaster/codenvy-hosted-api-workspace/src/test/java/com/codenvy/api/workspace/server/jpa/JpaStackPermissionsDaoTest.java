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

import com.codenvy.api.workspace.server.spi.jpa.JpaStackPermissionsDao;
import com.codenvy.api.workspace.server.stack.StackPermissionsImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.event.BeforeStackRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
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

import static java.util.Arrays.asList;
import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;
import static org.testng.Assert.assertTrue;

/**
 * @author Max Shaposhnik
 */
public class JpaStackPermissionsDaoTest {
    private EntityManager manager;

    private JpaStackPermissionsDao dao;

    private JpaStackPermissionsDao.RemovePermissionsBeforeStackRemovedEventSubscriber removePermissionsBeforeStackRemovedEventSubscriber;

    private StackPermissionsImpl[] permissions;
    private UserImpl[]             users;
    private StackImpl[]            stacks;

    @BeforeClass
    public void setupEntities() throws Exception {
        permissions = new StackPermissionsImpl[] {new StackPermissionsImpl("user1", "stack1", asList("read", "use", "run")),
                                                  new StackPermissionsImpl("user2", "stack1", asList("read", "use")),
                                                  new StackPermissionsImpl("user1", "stack2", asList("read", "run")),
                                                  new StackPermissionsImpl("user2", "stack2",
                                                                           asList("read", "use", "run", "configure"))};

        users = new UserImpl[] {new UserImpl("user1", "user1@com.com", "usr1"),
                                new UserImpl("user2", "user2@com.com", "usr2")};

        stacks = new StackImpl[] {
                new StackImpl("stack1", "st1", null, null, null, null, null, null, null, null),
                new StackImpl("stack2", "st2", null, null, null, null, null, null, null, null)};

        Injector injector = Guice.createInjector(new TestModule(), new OnPremisesJpaWorkspaceModule());
        manager = injector.getInstance(EntityManager.class);
        dao = injector.getInstance(JpaStackPermissionsDao.class);
        removePermissionsBeforeStackRemovedEventSubscriber =
                injector.getInstance(JpaStackPermissionsDao.RemovePermissionsBeforeStackRemovedEventSubscriber.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        manager.getTransaction().begin();
        for (UserImpl user : users) {
            manager.persist(user);
        }

        for (StackImpl stack : stacks) {
            manager.persist(stack);
        }

        for (StackPermissionsImpl stackPermissions : permissions) {
            manager.persist(stackPermissions);
        }
        manager.getTransaction().commit();
        manager.clear();
    }

    @AfterMethod
    public void cleanup() {
        manager.getTransaction().begin();

        manager.createQuery("SELECT p FROM StackPermissions p", StackPermissionsImpl.class)
               .getResultList()
               .forEach(manager::remove);

        manager.createQuery("SELECT r FROM Stack r", StackImpl.class)
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
    public void shouldStackPermissionsWhenStackIsRemoved() throws Exception {
        BeforeStackRemovedEvent event = new BeforeStackRemovedEvent(stacks[0]);
        removePermissionsBeforeStackRemovedEventSubscriber.onEvent(event);
        assertTrue(dao.getByInstance("stack1", 30, 0).isEmpty());
    }

    @Test
    public void shouldStoreStackPublicPermission() throws Exception {
        final StackPermissionsImpl publicPermission = new StackPermissionsImpl("*",
                                                                               "stack1",
                                                                               asList("read", "use", "run"));
        dao.store(publicPermission);

        assertTrue(dao.getByInstance(publicPermission.getInstanceId(), 30, 0)
                      .getItems()
                      .contains(new StackPermissionsImpl(publicPermission)));
    }

    @Test
    public void shouldUpdateExistingStackPublicPermissions() throws Exception {
        final StackPermissionsImpl publicPermission = new StackPermissionsImpl("*",
                                                                               "stack1",
                                                                               asList("read", "use", "run"));
        dao.store(publicPermission);
        dao.store(publicPermission);

        final Page<StackPermissionsImpl> permissions = dao.getByInstance(publicPermission.getInstanceId(), 30, 0);
        assertTrue(permissions.getItems().contains(new StackPermissionsImpl(publicPermission)));
        assertTrue(permissions.getItems().stream().filter(p -> "*".equals(p.getUserId())).count() == 1);
    }

    @Test
    public void shouldRemoveStackPublicPermission() throws Exception {
        final StackPermissionsImpl publicPermission = new StackPermissionsImpl("*",
                                                                               "stack1",
                                                                               asList("read", "use", "run"));
        dao.store(publicPermission);
        dao.remove(publicPermission.getUserId(), publicPermission.getInstanceId());

        Page<StackPermissionsImpl> byInstance = dao.getByInstance(publicPermission.getInstanceId(), 30, 0);
        assertTrue(byInstance.getItems().stream().filter(p -> "*".equals(p.getUserId())).count() == 0);
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
