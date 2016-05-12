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
package com.codenvy.api.permission.server;

import com.codenvy.api.permission.server.dao.PermissionsStorage;
import com.codenvy.api.permission.shared.Permissions;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link PermissionManager}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class PermissionManagerTest {
    @Mock
    private PermissionsStorage permissionsStorage;

    private PermissionManager permissionManager;

    @BeforeMethod
    public void setUp() throws Exception {
        when(permissionsStorage.getDomains()).thenReturn(ImmutableSet.of(new TestDomain()));

        permissionManager = new PermissionManager(ImmutableSet.of(permissionsStorage));
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Permissions Domain 'test' should be stored in only one storage. " +
                                            "Duplicated in class com.codenvy.api.permission.server.dao.PermissionsStorage.* and class com.codenvy.api.permission.server.dao.PermissionsStorage.*")
    public void shouldThrowExceptionIfThereAreTwoStoragesWhichServeOneDomain() throws Exception {
        PermissionsStorage anotherStorage = mock(PermissionsStorage.class);
        when(anotherStorage.getDomains()).thenReturn(ImmutableSet.of(new TestDomain()));

        permissionManager = new PermissionManager(ImmutableSet.of(permissionsStorage, anotherStorage));
    }

    @Test
    public void shouldBeAbleToStorePermissions() throws Exception {
        final PermissionsImpl permissions = new PermissionsImpl("user", "test", "test123", singletonList("setPermissions"));

        permissionManager.storePermission(permissions);

        verify(permissionsStorage).store(eq(permissions));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Can't edit permissions because there is not any another user with permission 'setPermissions'")
    public void shouldNotStorePermissionsWhenItRemoveLastSetPermissions() throws Exception {
        when(permissionsStorage.exists("user", "test", "test123", "setPermissions")).thenReturn(true);
        when(permissionsStorage.getByInstance("test", "test123"))
                .thenReturn(singletonList(new PermissionsImpl("user", "test", "test123", singletonList("delete"))));

        permissionManager.storePermission(new PermissionsImpl("user", "test", "test123", singletonList("delete")));
    }

    @Test
    public void shouldNotCheckExistingSetPermissionsIfUserDoesNotHaveItAtAll() throws Exception {
        when(permissionsStorage.exists("user", "test", "test123", "setPermissions")).thenReturn(false);
        when(permissionsStorage.getByInstance("test", "test123"))
                .thenReturn(singletonList(new PermissionsImpl("user", "test", "test123", singletonList("delete"))));

        permissionManager.storePermission(new PermissionsImpl("user", "test", "test123", singletonList("delete")));

        verify(permissionsStorage, never()).getByInstance(anyString(), anyString());
    }

    @Test
    public void shouldBeAbleToDeletePermissions() throws Exception {
        permissionManager.remove("user", "test", "test123");

        verify(permissionsStorage).remove(eq("user"), eq("test"), eq("test123"));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Can't remove permissions because there is not any another user with permission 'setPermissions'")
    public void shouldNotRemovePermissionsWhenItContainsLastSetPermissionsAction() throws Exception {
        when(permissionsStorage.exists("user", "test", "test123", "setPermissions")).thenReturn(true);
        when(permissionsStorage.getByInstance("test", "test123"))
                .thenReturn(singletonList(new PermissionsImpl("user", "test", "test123", singletonList("delete"))));

        permissionManager.remove("user", "test", "test123");
    }

    @Test
    public void shouldNotCheckExistingSetPermissionsIfUserDoesNotHaveItAtAllOnRemove() throws Exception {
        when(permissionsStorage.exists("user", "test", "test123", "setPermissions")).thenReturn(false);
        when(permissionsStorage.getByInstance("test", "test123"))
                .thenReturn(singletonList(new PermissionsImpl("user", "test", "test123", singletonList("delete"))));

        permissionManager.remove("user", "test", "test123");

        verify(permissionsStorage, never()).getByInstance(anyString(), anyString());
    }

    @Test
    public void shouldBeAbleToGetPermissionsByUserAndDomainAndInstance() throws Exception {
        final PermissionsImpl permissions = new PermissionsImpl("user", "test", "test123", singletonList("read"));
        when(permissionsStorage.get("user", "test", "test123")).thenReturn(permissions);

        final Permissions fetchedPermissions = permissionManager.get("user", "test", "test123");

        assertEquals(permissions, fetchedPermissions);
    }

    @Test
    public void shouldBeAbleToGetPermissionsByInstance() throws Exception {
        final PermissionsImpl firstPermissions = new PermissionsImpl("user", "test", "test123", singletonList("read"));
        final PermissionsImpl secondPermissions = new PermissionsImpl("user1", "test", "test123", singletonList("read"));

        when(permissionsStorage.getByInstance("test", "test123")).thenReturn(Arrays.asList(firstPermissions, secondPermissions));

        final List<PermissionsImpl> fetchedPermissions = permissionManager.getByInstance("test", "test123");

        assertEquals(fetchedPermissions.size(), 2);
        assertTrue(fetchedPermissions.contains(firstPermissions));
        assertTrue(fetchedPermissions.contains(secondPermissions));
    }

    @Test
    public void shouldBeAbleToCheckPermissionExistence() throws Exception {
        when(permissionsStorage.exists("user", "test", "test123", "use")).thenReturn(true);
        when(permissionsStorage.exists("user", "test", "test123", "update")).thenReturn(false);

        assertTrue(permissionManager.exists("user", "test", "test123", "use"));
        assertFalse(permissionManager.exists("user", "test", "test123", "update"));
    }

    @Test
    public void shouldBeAbleToDomains() throws Exception {
        final Set<String> domains = permissionManager.getDomains();

        assertEquals(domains.size(), 1);
        assertTrue(domains.contains("test"));
    }

    @Test
    public void shouldBeAbleToDomainsActions() throws Exception {
        final Set<String> domains = permissionManager.getDomainsActions("test");

        assertEquals(domains.size(), 6);
        assertTrue(domains.containsAll(ImmutableSet.of("setPermissions", "readPermissions", "read", "write", "use", "delete")));
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Requested unsupported domain 'unsupported'")
    public void shouldThrowExceptionWhenRequestedUnsupportedDomain() throws Exception {
        permissionManager.getDomainsActions("unsupported");
    }

    public class TestDomain extends PermissionsDomain {
        public TestDomain() {
            super("test", ImmutableSet.of("setPermissions", "readPermissions", "read", "write", "use", "delete"));
        }
    }
}
