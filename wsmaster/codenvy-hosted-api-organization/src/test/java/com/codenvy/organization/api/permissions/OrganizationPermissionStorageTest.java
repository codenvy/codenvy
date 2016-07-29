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
package com.codenvy.organization.api.permissions;

import com.codenvy.organization.spi.impl.MemberImpl;
import com.codenvy.organization.spi.MemberDao;
import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.PermissionsImpl;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.organization.api.permissions.OrganizationPermissionStorage;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.NotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.codenvy.organization.api.permissions.OrganizationDomain.DELETE;
import static com.codenvy.organization.api.permissions.OrganizationDomain.DOMAIN_ID;
import static com.codenvy.organization.api.permissions.OrganizationDomain.MANAGE_RESOURCES;
import static com.codenvy.organization.api.permissions.OrganizationDomain.UPDATE;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link OrganizationPermissionStorage}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationPermissionStorageTest {
    @Mock
    private MemberDao memberDao;

    @InjectMocks
    private OrganizationPermissionStorage permissionStorage;

    @Test
    public void shouldBeAbleToStorePermissions() throws Exception {
        permissionStorage.store(new PermissionsImpl("user123",
                                                    "organization",
                                                    "organization123",
                                                    asList(DELETE, UPDATE)));

        verify(memberDao).store(eq(new MemberImpl("user123", "organization123", asList(DELETE, UPDATE))));
    }

    @Test
    public void shouldBeAbleToGetPermissionsByUserAndDomainAndInstance() throws Exception {
        when(memberDao.getMember(anyString(), anyString()))
                .thenReturn(new MemberImpl("user123", "organization123", asList(DELETE, UPDATE)));

        PermissionsImpl result = permissionStorage.get("user123", DOMAIN_ID, "organization123");

        verify(memberDao).getMember(eq("organization123"), eq("user123"));
        assertEquals(result, new PermissionsImpl("user123",
                                                 "organization",
                                                 "organization123",
                                                 asList(DELETE, UPDATE)));
    }

    @Test
    public void shouldBeAbleToGetPermissionsByDomainAndInstance() throws Exception {
        when(memberDao.getMembers(anyString()))
                .thenReturn(Collections.singletonList(new MemberImpl("user123", "organization123", asList(DELETE, UPDATE))));

        List<PermissionsImpl> result = permissionStorage.getByInstance(DOMAIN_ID, "organization123");

        assertEquals(result.size(), 1);
        verify(memberDao).getMembers(eq("organization123"));
        assertEquals(result.get(0), new PermissionsImpl("user123",
                                                        "organization",
                                                        "organization123",
                                                        asList(DELETE, UPDATE)));
    }

    @Test
    public void shouldBeAbleToCheckPermissionExistence() throws Exception {
        when(memberDao.getMember(anyString(), anyString()))
                .thenReturn(new MemberImpl("user123", "organization123", asList(DELETE,
                                                                                UPDATE)));

        boolean existence = permissionStorage.exists("user123", DOMAIN_ID, "organization123", DELETE);
        boolean nonExistence = permissionStorage.exists("user123", DOMAIN_ID, "organization123", MANAGE_RESOURCES);

        assertTrue(existence);
        assertFalse(nonExistence);
    }

    @Test
    public void shouldReturnFalseOnCheckPermissionExistenceWhenWorkerDoesNotExist() throws Exception {
        when(memberDao.getMember(anyString(), anyString())).thenThrow(new NotFoundException(""));

        boolean result = permissionStorage.exists("user123", DOMAIN_ID, "organization123", DELETE);

        assertFalse(result);
    }

    @Test
    public void shouldBeAbleToRemovePermissions() throws Exception {
        permissionStorage.remove("user123", DOMAIN_ID, "organization123");

        verify(memberDao).remove(eq("organization123"), eq("user123"));
    }

    @Test
    public void shouldReturnWorkspaceDomain() {
        Set<AbstractPermissionsDomain> supportedDomains = permissionStorage.getDomains();

        assertEquals(supportedDomains, ImmutableSet.of(new OrganizationDomain()));
    }
}
