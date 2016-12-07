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
package com.codenvy.api.machine.server.spi.tck;

import com.codenvy.api.machine.server.recipe.RecipePermissionsImpl;
import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.permission.shared.model.Permissions;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Max Shaposhnik
 */
@Listeners(TckListener.class)
@Test(suiteName = "RecipePermissionsDaoTck")
public class RecipePermissionsDaoTest {
    @Inject
    private PermissionsDao<RecipePermissionsImpl> dao;

    @Inject
    private TckRepository<RecipePermissionsImpl> permissionsRepository;

    @Inject
    private TckRepository<UserImpl>   userRepository;
    @Inject
    private TckRepository<RecipeImpl> recipeRepository;

    RecipePermissionsImpl[] permissions;

    @BeforeMethod
    public void setUp() throws TckRepositoryException {
        permissions = new RecipePermissionsImpl[] {new RecipePermissionsImpl("user", "recipe1", asList("read", "use", "run")),
                                                   new RecipePermissionsImpl("user2", "recipe1", asList("read", "use", "run")),
                                                   new RecipePermissionsImpl("user3", "recipe1", asList("read", "use")),
                                                   new RecipePermissionsImpl("user2", "recipe2", asList("read", "run")),
                                                   new RecipePermissionsImpl("user3", "recipe2",
                                                                             asList("read", "use", "run", "configure"))
        };


        final UserImpl[] users = new UserImpl[] {new UserImpl("user", "user@com.com", "usr"),
                                                 new UserImpl("user2", "user2@com.com", "usr2"),
                                                 new UserImpl("user3", "user3@com.com", "usr3")};
        userRepository.createAll(asList(users));

        recipeRepository.createAll(
                asList(new RecipeImpl("recipe1", "rc1", null, null, null, null, null),
                       new RecipeImpl("recipe2", "rc2", null, null, null, null, null)
                ));

        permissionsRepository.createAll(Stream.of(permissions)
                                              .map(RecipePermissionsImpl::new)
                                              .collect(Collectors.toList()));
    }

    @AfterMethod
    public void cleanUp() throws TckRepositoryException {
        permissionsRepository.removeAll();
        recipeRepository.removeAll();
        userRepository.removeAll();
    }

    /* RecipePermissionsDao.store() tests */
    @Test
    public void shouldStorePermissions() throws Exception {
        final RecipePermissionsImpl permissions = new RecipePermissionsImpl("user", "recipe1", asList("read", "use"));

        dao.store(permissions);

        final Permissions result = dao.get(permissions.getUserId(), permissions.getInstanceId());
        assertEquals(permissions, result);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenStoringArgumentIsNull() throws Exception {
        dao.store(null);
    }

    @Test
    public void shouldReplacePermissionsOnStoringWhenItHasAlreadyExisted() throws Exception {
        RecipePermissionsImpl oldPermissions = permissions[0];

        RecipePermissionsImpl newPermissions = new RecipePermissionsImpl(oldPermissions.getUserId(),
                                                                         oldPermissions.getInstanceId(),
                                                                         singletonList("read"));
        dao.store(newPermissions);

        final Permissions result = dao.get(oldPermissions.getUserId(), oldPermissions.getInstanceId());

        assertEquals(newPermissions, result);
    }

    @Test
    public void shouldReturnsSupportedDomainsIds() {
        //given
        AbstractPermissionsDomain<RecipePermissionsImpl> recipeDomain = new TestDomain();

        //then
        assertEquals(dao.getDomain(), recipeDomain);
    }

    /* RecipePermissionsDao.remove() tests */
    @Test
    public void shouldRemovePermissions() throws Exception {
        RecipePermissionsImpl testPermission = permissions[3];

        dao.remove(testPermission.getUserId(), testPermission.getInstanceId());

        assertFalse(dao.exists(testPermission.getUserId(), testPermission.getInstanceId(), testPermission.getActions().get(0)));
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Permissions on recipe 'instance' of user 'user' was not found.")
    public void shouldThrowNotFoundExceptionWhenPermissionsWasNotFoundOnRemove() throws Exception {
        dao.remove("user", "instance");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenRemovePermissionsUserIdArgumentIsNull() throws Exception {
        dao.remove(null, "instance");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenRemovePermissionsInstanceIdArgumentIsNull() throws Exception {
        dao.remove("user", null);
    }

    /* RecipePermissionsDao.getByInstance() tests */
    @Test
    public void shouldGetPermissionsByInstance() throws Exception {

        final Page<RecipePermissionsImpl> permissionsPage = dao.getByInstance(permissions[2].getInstanceId(), 1, 1);

        assertEquals(1, permissionsPage.getItemsCount());
        assertEquals(3, permissionsPage.getTotalItemsCount());
        final List<RecipePermissionsImpl> fetchedPermissions = permissionsPage.getItems();
        assertTrue(fetchedPermissions.contains(permissions[0])
                   ^ fetchedPermissions.contains(permissions[1])
                   ^ fetchedPermissions.contains(permissions[2]));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenGetByInstanceInstanceIdArgumentIsNull() throws Exception {
        dao.getByInstance(null, 30, 0);
    }

    /* RecipePermissionsDao.get() tests */
    @Test
    public void shouldBeAbleToGetPermissions() throws Exception {
        final RecipePermissionsImpl result1 = dao.get(permissions[0].getUserId(), permissions[0].getInstanceId());
        final RecipePermissionsImpl result2 = dao.get(permissions[2].getUserId(), permissions[2].getInstanceId());

        assertEquals(result1, permissions[0]);
        assertEquals(result2, permissions[2]);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Permissions on recipe 'instance' of user 'user' was not found.")
    public void shouldThrowNotFoundExceptionWhenThereIsNotAnyPermissionsForGivenUserAndDomainAndInstance() throws Exception {
        dao.get("user", "instance");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenGetPermissionsUserIdArgumentIsNull() throws Exception {
        dao.get(null, "instance");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenGetPermissionsInstanceIdArgumentIsNull() throws Exception {
        dao.get("user", null);
    }

    /* RecipePermissionsDao.exists() tests */
    @Test
    public void shouldBeAbleToCheckPermissionExistence() throws Exception {

        RecipePermissionsImpl testPermission = permissions[0];

        final boolean readPermissionExisted = dao.exists(testPermission.getUserId(), testPermission.getInstanceId(), "read");
        final boolean fakePermissionExisted = dao.exists(testPermission.getUserId(), testPermission.getInstanceId(), "fake");

        assertEquals(readPermissionExisted, testPermission.getActions().contains("read"));
        assertEquals(fakePermissionExisted, testPermission.getActions().contains("fake"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenPermissionsExistsUserIdArgumentIsNull() throws Exception {
        dao.exists(null, "instance", "action");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenPermissionsExistsInstanceIdArgumentIsNull() throws Exception {
        dao.exists("user", null, "action");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenPermissionsExistsActionArgumentIsNull() throws Exception {
        dao.exists("user", "instance", null);
    }

    public static class TestDomain extends AbstractPermissionsDomain<RecipePermissionsImpl> {
        public TestDomain() {
            super("recipe", asList("read", "write", "use"));
        }

        @Override
        protected RecipePermissionsImpl doCreateInstance(String userId, String instanceId, List allowedActions) {
            return null;
        }
    }
}
