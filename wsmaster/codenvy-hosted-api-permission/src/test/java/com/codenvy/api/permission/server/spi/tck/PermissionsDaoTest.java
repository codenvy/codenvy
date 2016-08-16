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
package com.codenvy.api.permission.server.spi.tck;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.model.impl.PermissionsImpl;
import com.codenvy.api.permission.server.model.impl.RecipePermissionsImpl;
import com.codenvy.api.permission.server.model.impl.StackPermissionsImpl;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.permission.shared.model.Permissions;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Compatibility test for {@link PermissionsDao}
 *
 * @author Max Shaposhnik
 */
@Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = "PermissionsDaoTck")
public class PermissionsDaoTest {
    @Inject
    private PermissionsDao permissionsDao;

    @Inject
    private TckRepository<PermissionsImpl> permissionsRepository;

    @Inject
    private TckRepository<UserImpl> userRepository;

    @Inject
    private TckRepository<StackImpl> stackRepository;

    @Inject
    private TckRepository<RecipeImpl> recipeRepository;


    PermissionsImpl[] permissions;

    @BeforeMethod
    public void setUp() throws TckRepositoryException {
        permissions = new PermissionsImpl[]{new StackPermissionsImpl("user1", "stack1", Arrays.asList("read", "use", "run")),
                                            new StackPermissionsImpl("user2", "stack2", Arrays.asList("read", "use")),
                                            new RecipePermissionsImpl("user1", "recipe1", Arrays.asList("read", "run")),
                                            new RecipePermissionsImpl("user2", "recipe2", Arrays.asList("read", "use", "run", "configure"))};

        userRepository.createAll(Arrays.asList(new UserImpl("user", "user@com.com", "usr"),
                                               new UserImpl("user1", "user1@com.com", "usr1"),
                                               new UserImpl("user2", "user2@com.com", "usr2")));

        stackRepository.createAll(Arrays.asList(new StackImpl("stack1", "st1", null, null, null, null, null, null, null, null),
                                                new StackImpl("stack2", "st2", null, null, null, null, null, null, null, null)));

        recipeRepository.createAll(Arrays.asList(new RecipeImpl("recipe1", null, null, null, null, null, null),
                                                 new RecipeImpl("recipe2", null, null, null, null, null, null)));


        permissionsRepository.createAll(Arrays.asList(permissions));

    }

    @AfterMethod
    public void cleanUp() throws TckRepositoryException {
        stackRepository.removeAll();
        recipeRepository.removeAll();
        permissionsRepository.removeAll();
        userRepository.removeAll();
    }


    /* PermissionsDao.store() tests */
    @Test
    public void shouldStorePermissions() throws Exception {
        final PermissionsImpl permissions = new StackPermissionsImpl("user", "stack", Arrays.asList("read", "use"));

        permissionsDao.store(permissions);

        final Permissions result = permissionsDao.get("user", "stack", permissions.getInstanceId());
        assertEquals(permissions, result);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenStoringArgumentIsNull() throws Exception {
        permissionsDao.store(null);
    }

    @Test
    public void shouldReplacePermissionsOnStoringWhenItHasAlreadyExisted() throws Exception {

        Permissions oldPermissions = permissions[0];

        PermissionsImpl newPermissions =
                new PermissionsImpl(oldPermissions.getUserId(), oldPermissions.getDomainId(), oldPermissions.getInstanceId(),
                                    singletonList("read"));
        permissionsDao.store(newPermissions);

        final Permissions result = permissionsDao.get("user1", "stack", oldPermissions.getInstanceId());

        assertEquals(newPermissions, result);
    }

    @Test
    public void shouldReturnsSupportedDomainsIds() {
        assertEquals(permissionsDao.getDomains(), ImmutableSet.of(new TestDomain()));
    }



    /* PermissionsDao.remove() tests */
    @Test
    public void shouldRemovePermissions() throws Exception {
        PermissionsImpl testPermission = permissions[3];

        permissionsDao.remove(testPermission.getUserId(), testPermission.getDomainId(), testPermission.getInstanceId());

        assertFalse(permissionsDao.exists(testPermission.getUserId(), testPermission.getDomainId(), testPermission.getInstanceId(), testPermission.getActions().get(0)));
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Permissions of user 'user' on domain 'domain' with id 'instance' was not found.")
    public void shouldThrowNotFoundExceptionWhenPermissionsWasNotFoundOnRemove() throws Exception {
        permissionsDao.remove("user", "domain", "instance");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenRemovePermissionsDomainIdArgumentIsNull() throws Exception {
        permissionsDao.remove("user", null, "instance");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenRemovePermissionsUserIdArgumentIsNull() throws Exception {
        permissionsDao.remove(null, "domain", "instance");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenRemovePermissionsInstanceIdArgumentIsNull() throws Exception {
        permissionsDao.remove("user", "domain", null);
    }

    /* PermissionsDao.getByInstance() tests */
    @Test
    public void shouldGetPermissionsByInstance() throws Exception {

        final List<PermissionsImpl> result = permissionsDao.getByInstance(permissions[2].getDomainId(), permissions[2].getInstanceId());

        assertEquals(result.size(), 1);
        assertEquals(permissions[2],result.get(0));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenGetByInstanceDomainIdArgumentIsNull() throws Exception {
        permissionsDao.getByInstance(null, "instance");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenGetByInstanceInstanceIdArgumentIsNull() throws Exception {
        permissionsDao.getByInstance("domain", null);
    }


    /* PermissionsDao.get() tests */
    @Test
    public void shouldBeAbleToGetPermissions() throws Exception {

        final Permissions result1 = permissionsDao.get(permissions[0].getUserId(), permissions[0].getDomainId(),
                                                       permissions[0].getInstanceId());
        final Permissions result2 = permissionsDao.get(permissions[2].getUserId(), permissions[2].getDomainId(),
                                                       permissions[2].getInstanceId());

        assertTrue(result1 instanceof StackPermissionsImpl);
        assertEquals(result1, permissions[0]);

        assertTrue(result2 instanceof RecipePermissionsImpl);
        assertEquals(result2, permissions[2]);

    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Permissions of user 'user' on domain 'domain' with id 'instance' was not found.")
    public void shouldThrowNotFoundExceptionWhenThereIsNotAnyPermissionsForGivenUserAndDomainAndInstance() throws Exception {
         permissionsDao.get("user", "domain", "instance");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenGetPermissionsDomainIdArgumentIsNull() throws Exception {
        permissionsDao.get("user", null, "instance");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenGetPermissionsUserIdArgumentIsNull() throws Exception {
        permissionsDao.get(null, "domain", "instance");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenGetPermissionsInstanceIdArgumentIsNull() throws Exception {
        permissionsDao.get("user", "domain", null);
    }

    /* PermissionsDao.exists() tests */
    @Test
    public void shouldBeAbleToCheckPermissionExistence() throws Exception {

        PermissionsImpl testPermission = permissions[0];

        final boolean readPermissionExisted =
                permissionsDao.exists(testPermission.getUserId(), testPermission.getDomainId(), testPermission.getInstanceId(), "read");
        final boolean fakePermissionExisted =
                permissionsDao.exists(testPermission.getUserId(), testPermission.getDomainId(), testPermission.getInstanceId(), "fake");

        assertEquals(readPermissionExisted, testPermission.getActions().contains("read"));
        assertEquals(fakePermissionExisted, testPermission.getActions().contains("fake"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenPermissionsExistsDomainIdArgumentIsNull() throws Exception {
        permissionsDao.exists("user", null, "instance", "action");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenPermissionsExistsUserIdArgumentIsNull() throws Exception {
        permissionsDao.exists(null, "domain", "instance", "action");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenPermissionsExistsInstanceIdArgumentIsNull() throws Exception {
        permissionsDao.exists("user", "domain", null, "action");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenPermissionsExistsActionArgumentIsNull() throws Exception {
        permissionsDao.exists("user", "domain", "instance", null);
    }

    public static class TestDomain extends AbstractPermissionsDomain {
        public TestDomain() {
            super("test", Arrays.asList("read", "write", "use", "delete"));
        }
    }

}
