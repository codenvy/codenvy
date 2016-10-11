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
package com.codenvy.api.machine.server.jpa;

import com.codenvy.api.machine.server.jpa.JpaRecipePermissionsDao.RemovePermissionsBeforeRecipeRemovedEventSubscriber;
import com.codenvy.api.machine.server.recipe.RecipePermissionsImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.machine.server.jpa.JpaRecipeDao;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
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
import static java.util.Collections.singletonList;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link RemovePermissionsBeforeRecipeRemovedEventSubscriber}
 *
 * @author Sergii Leschenko
 */
public class RemovePermissionsBeforeRecipeRemovedEventSubscriberTest {
    private EntityManager           manager;
    private JpaRecipeDao            recipeDao;
    private JpaRecipePermissionsDao recipePermissionsDao;

    private RemovePermissionsBeforeRecipeRemovedEventSubscriber subscriber;

    private RecipeImpl              recipe;
    private UserImpl[]              users;
    private RecipePermissionsImpl[] recipePermissions;

    @BeforeClass
    public void setupEntities() throws Exception {
        recipe = new RecipeImpl("recipe1", "test", "creator", "dockerfile", "FROM test", singletonList("test"), "test recipe");
        users = new UserImpl[3];
        for (int i = 0; i < 3; i++) {
            users[i] = new UserImpl("user" + i, "user" + i + "@test.com", "username" + i);
        }
        recipePermissions = new RecipePermissionsImpl[3];
        for (int i = 0; i < 3; i++) {
            recipePermissions[i] = new RecipePermissionsImpl(users[i].getId(), recipe.getId(), asList("read", "update"));
        }

        Injector injector = Guice.createInjector(new OnPremisesJpaMachineModule(), new TestModule());

        manager = injector.getInstance(EntityManager.class);
        recipeDao = injector.getInstance(JpaRecipeDao.class);
        recipePermissionsDao = injector.getInstance(JpaRecipePermissionsDao.class);

        subscriber = injector.getInstance(RemovePermissionsBeforeRecipeRemovedEventSubscriber.class);
        subscriber.subscribe();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        manager.getTransaction().begin();
        manager.persist(recipe);
        Stream.of(users).forEach(manager::persist);
        Stream.of(recipePermissions).forEach(manager::persist);
        manager.getTransaction().commit();
    }

    @AfterMethod
    public void cleanup() {
        manager.getTransaction().begin();
        manager.createQuery("SELECT recipePermissions FROM RecipePermissions recipePermissions", RecipePermissionsImpl.class)
               .getResultList()
               .forEach(manager::remove);
        manager.createQuery("SELECT recipe FROM Recipe recipe", RecipeImpl.class)
               .getResultList()
               .forEach(manager::remove);
        manager.createQuery("SELECT usr FROM Usr usr", UserImpl.class)
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
        recipeDao.remove(recipe.getId());

        assertEquals(recipePermissionsDao.getByInstance(recipe.getId(), 1, 0).getTotalItemsCount(), 0);
    }

    @Test
    public void shouldRemoveAllRecipePermissionsWhenPageSizeEqualsToOne() throws Exception {
        subscriber.removeRecipePermissions(recipe.getId(), 1);

        assertEquals(recipePermissionsDao.getByInstance(recipe.getId(), 1, 0).getTotalItemsCount(), 0);
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
