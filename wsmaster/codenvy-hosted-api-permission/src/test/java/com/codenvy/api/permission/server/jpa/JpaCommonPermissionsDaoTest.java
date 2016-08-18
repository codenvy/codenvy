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
package com.codenvy.api.permission.server.jpa;

import com.codenvy.api.permission.server.model.impl.PermissionsImpl;
import com.codenvy.api.permission.server.spi.tck.TestPermissionsImpl1;
import com.codenvy.api.permission.server.spi.tck.TestPermissionsImpl2;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * JPA-specific permission DAO test
 *
 * @author Max Shaposhnik
 */
public class JpaCommonPermissionsDaoTest {

    private EntityManager manager;

    private JpaCommonPermissionsDao permissionsDao;

    private JpaCommonPermissionsDao.RemovePermissionsBeforeUserRemovedEventSubscriber removePermissionsBeforeUserRemovedEventSubscriber;

    PermissionsImpl[] permissionses;

    UserImpl[] users;


    @BeforeClass
    public void setupEntities() throws Exception {
        permissionses =
                new PermissionsImpl[]{new TestPermissionsImpl1("user1", "domain1", "instance1", Arrays.asList("read", "use", "run")),
                                      new TestPermissionsImpl1("user2", "domain1", "instance2", Arrays.asList("read", "use")),
                                      new TestPermissionsImpl2("user1", "domain2", "instance1", Arrays.asList("read", "run")),
                                      new TestPermissionsImpl2("user2", "domain2", "instance2",
                                                               Arrays.asList("read", "use", "run", "configure"))};

        users = new UserImpl[]{new UserImpl("user1", "user1@com.com", "usr1"),
                               new UserImpl("user2", "user2@com.com", "usr2")};

        Injector injector = Guice.createInjector(new PermissionsTckModule(), new PermissionsJpaModule());
        manager = injector.getInstance(EntityManager.class);
        permissionsDao = injector.getInstance(JpaCommonPermissionsDao.class);
        removePermissionsBeforeUserRemovedEventSubscriber = injector.getInstance(
                JpaCommonPermissionsDao.RemovePermissionsBeforeUserRemovedEventSubscriber.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        manager.getTransaction().begin();
        for (UserImpl user : users) {
            manager.persist(user);
        }

        for (PermissionsImpl ws : permissionses) {
            manager.persist(ws);
        }

        manager.getTransaction().commit();
        manager.clear();
    }

    @AfterMethod
    public void cleanup() {
        manager.getTransaction().begin();

        manager.createQuery("SELECT e FROM TestPermissionsImpl1 e", PermissionsImpl.class)
               .getResultList()
               .forEach(manager::remove);

        manager.createQuery("SELECT e FROM TestPermissionsImpl2 e", PermissionsImpl.class)
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
    public void shouldRemovePermissionsWhenUserIsRemoved() throws Exception {
        BeforeUserRemovedEvent event =  new BeforeUserRemovedEvent(new UserImpl("user1", "email@co.com", "user"));
        removePermissionsBeforeUserRemovedEventSubscriber.onEvent(event);

        List<PermissionsImpl> permsByUser1 =
                manager.createQuery("SELECT e FROM TestPermissionsImpl1 e where e.userId = :userId", PermissionsImpl.class)
                       .setParameter("userId", "user1")
                       .getResultList();

        List<PermissionsImpl> permsByUser2 =
                manager.createQuery("SELECT e FROM TestPermissionsImpl2 e where e.userId = :userId", PermissionsImpl.class)
                       .setParameter("userId", "user1")
                       .getResultList();

        assertTrue(permsByUser1.isEmpty() && permsByUser2.isEmpty());
    }

}
