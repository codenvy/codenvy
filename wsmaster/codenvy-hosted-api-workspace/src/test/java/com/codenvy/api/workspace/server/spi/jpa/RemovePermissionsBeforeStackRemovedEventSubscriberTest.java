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

import com.codenvy.api.machine.server.recipe.RecipePermissionsImpl;
import com.codenvy.api.workspace.server.jpa.OnPremisesJpaWorkspaceModule;
import com.codenvy.api.workspace.server.spi.jpa.JpaStackPermissionsDao.RemovePermissionsBeforeStackRemovedEventSubscriber;
import com.codenvy.api.workspace.server.stack.StackPermissionsImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link RemovePermissionsBeforeStackRemovedEventSubscriber}
 *
 * @author Sergii Leschenko
 */
public class RemovePermissionsBeforeStackRemovedEventSubscriberTest {
    private EntityManager          manager;
    private JpaStackDao            stackDao;
    private JpaStackPermissionsDao stackPermissionsDao;

    private RemovePermissionsBeforeStackRemovedEventSubscriber subscriber;

    private StackImpl              stack;
    private UserImpl[]             users;
    private StackPermissionsImpl[] stackPermissions;

    @BeforeClass
    public void setupEntities() throws Exception {
        stack = StackImpl.builder()
                         .setId("stack123")
                         .setName("defaultStack")
                         .build();
        users = new UserImpl[3];
        for (int i = 0; i < 3; i++) {
            users[i] = new UserImpl("user" + i, "user" + i + "@test.com", "username" + i);
        }
        stackPermissions = new StackPermissionsImpl[3];
        for (int i = 0; i < 3; i++) {
            stackPermissions[i] = new StackPermissionsImpl(users[i].getId(), stack.getId(), asList("read", "update"));
        }

        Injector injector = Guice.createInjector(new OnPremisesJpaWorkspaceModule(), new TestModule());

        manager = injector.getInstance(EntityManager.class);
        stackDao = injector.getInstance(JpaStackDao.class);
        stackPermissionsDao = injector.getInstance(JpaStackPermissionsDao.class);

        subscriber = injector.getInstance(RemovePermissionsBeforeStackRemovedEventSubscriber.class);
        subscriber.subscribe();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        manager.getTransaction().begin();
        manager.persist(stack);
        Stream.of(users).forEach(manager::persist);
        Stream.of(stackPermissions).forEach(manager::persist);
        manager.getTransaction().commit();
    }

    @AfterMethod
    public void cleanup() {
        manager.getTransaction().begin();
        manager.createQuery("SELECT recipe FROM Recipe recipe", RecipeImpl.class)
               .getResultList()
               .forEach(manager::remove);
        manager.createQuery("SELECT usr FROM Usr usr", UserImpl.class)
               .getResultList()
               .forEach(manager::remove);
        manager.createQuery("SELECT recipePermissions FROM RecipePermissions recipePermissions", RecipePermissionsImpl.class)
               .getResultList()
               .forEach(manager::remove);
        manager.getTransaction().commit();
    }

    @AfterClass
    public void shutdown() throws Exception {
        subscriber.unsubscribe();
        manager.getEntityManagerFactory().close();
    }

    @Test
    public void shouldRemoveAllRecipePermissionsWhenRecipeIsRemoved() throws Exception {
        stackDao.remove(stack.getId());

        assertEquals(stackPermissionsDao.getByInstance(stack.getId(), 1, 0).getTotalItemsCount(), 0);
    }

    @Test
    public void shouldRemoveAllRecipePermissionsWhenPageSizeEqualsToOne() throws Exception {
        subscriber.removeStackPermissions(stack.getId(), 1);

        assertEquals(stackPermissionsDao.getByInstance(stack.getId(), 1, 0).getTotalItemsCount(), 0);
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            Map<String, String> properties = new HashMap<>();
            if (System.getProperty("jdbc.driver") != null) {
                properties.put(JDBC_DRIVER, System.getProperty("jdbc.driver"));
                properties.put(JDBC_URL, System.getProperty("jdbc.url"));
                properties.put(JDBC_USER, System.getProperty("jdbc.user"));
                properties.put(JDBC_PASSWORD, System.getProperty("jdbc.password"));
            }
            JpaPersistModule main = new JpaPersistModule("main");
            main.properties(properties);
            install(main);
            bind(JpaInitializer.class).asEagerSingleton();
            bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();
        }
    }
}
