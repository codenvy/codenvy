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

import com.codenvy.api.machine.server.recipe.RecipePermissionsImpl;
import com.codenvy.api.permission.server.PermissionsModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
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
public class JpaRecipePermissionsDaoTest {
    private EntityManager           manager;
    private JpaRecipePermissionsDao dao;

    private RecipePermissionsImpl[] permissions;
    private UserImpl[]              users;
    private RecipeImpl[]            recipes;

    @BeforeClass
    public void setupEntities() throws Exception {
        permissions = new RecipePermissionsImpl[] {new RecipePermissionsImpl("user1", "recipe1", asList("read", "use", "run")),
                                                   new RecipePermissionsImpl("user2", "recipe1", asList("read", "use")),
                                                   new RecipePermissionsImpl("user1", "recipe2", asList("read", "run")),
                                                   new RecipePermissionsImpl("user2", "recipe2",
                                                                             asList("read", "use", "run", "configure"))};

        users = new UserImpl[] {new UserImpl("user1", "user1@com.com", "usr1"),
                                new UserImpl("user2", "user2@com.com", "usr2")};

        recipes = new RecipeImpl[] {new RecipeImpl("recipe1", "rc1", null, null, null, null, null),
                                    new RecipeImpl("recipe2", "rc2", null, null, null, null, null)};

        Injector injector = Guice.createInjector(new TestModule(), new OnPremisesJpaMachineModule(), new PermissionsModule());
        manager = injector.getInstance(EntityManager.class);
        dao = injector.getInstance(JpaRecipePermissionsDao.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        manager.getTransaction().begin();
        for (UserImpl user : users) {
            manager.persist(user);
        }

        for (RecipeImpl recipe : recipes) {
            manager.persist(recipe);
        }

        for (RecipePermissionsImpl recipePermissions : permissions) {
            manager.persist(recipePermissions);
        }
        manager.getTransaction().commit();
        manager.clear();
    }

    @AfterMethod
    public void cleanup() {
        manager.getTransaction().begin();

        manager.createQuery("SELECT p FROM RecipePermissions p", RecipePermissionsImpl.class)
               .getResultList()
               .forEach(manager::remove);

        manager.createQuery("SELECT r FROM Recipe r", RecipeImpl.class)
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
    public void shouldGetRecipePermissionByInstanceIdAndWildcard() throws Exception {
        manager.getTransaction().begin();
        manager.persist(new RecipePermissionsImpl(null, "recipe1", asList("read", "use", "run")));
        manager.getTransaction().commit();

        RecipePermissionsImpl result = dao.get("*", "recipe1");
        assertEquals(result.getInstanceId(), "recipe1");
        assertEquals(result.getUserId(), null);
    }

    @Test
    public void shouldGetRecipePermissionByInstanceIdAndUserIdIfPublicPermissionExistsWithSameInstanceId() throws Exception {
        manager.getTransaction().begin();
        manager.persist(new RecipePermissionsImpl(null, "recipe1", asList("read", "use", "run")));
        manager.getTransaction().commit();

        RecipePermissionsImpl result = dao.get("user1", "recipe1");
        assertEquals(result.getInstanceId(), "recipe1");
        assertEquals(result.getUserId(), "user1");
    }

    @Test
    public void shouldStoreRecipePublicPermission() throws Exception {
        //given
        final RecipePermissionsImpl publicPermission = new RecipePermissionsImpl("*",
                                                                                 "recipe1",
                                                                                 asList("read", "use", "run"));

        //when
        dao.store(publicPermission);

        //then
        assertTrue(dao.getByInstance(publicPermission.getInstanceId(), 3, 0)
                      .getItems()
                      .contains(new RecipePermissionsImpl(publicPermission)));
    }

    @Test
    public void shouldUpdateExistingRecipePublicPermissions() throws Exception {
        final RecipePermissionsImpl publicPermission = new RecipePermissionsImpl("*",
                                                                                 "recipe1",
                                                                                 asList("read", "use", "run"));
        dao.store(publicPermission);
        dao.store(publicPermission);

        final List<RecipePermissionsImpl> storedPermissions = dao.getByInstance(publicPermission.getInstanceId(), 30, 0)
                                                                 .getItems();
        assertTrue(storedPermissions.contains(new RecipePermissionsImpl(publicPermission)));
        assertTrue(storedPermissions.stream().filter(p -> "*".equals(p.getUserId())).count() == 1);
    }

    @Test
    public void shouldRemoveRecipePublicPermission() throws Exception {
        final RecipePermissionsImpl publicPermission = new RecipePermissionsImpl("*",
                                                                                 "recipe1",
                                                                                 asList("read", "use", "run"));
        dao.store(publicPermission);
        dao.remove(publicPermission.getUserId(), publicPermission.getInstanceId());

        final List<RecipePermissionsImpl> storePermissions = dao.getByInstance(publicPermission.getInstanceId(), 30, 0)
                                                                .getItems();
        assertTrue(storePermissions.stream().filter(p -> "*".equals(p.getUserId())).count() == 0);
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
