/*
 *  [2012] - [2017] Codenvy, S.A.
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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Listeners(MockitoTestNGListener.class)
public class PermissionCheckerImplTest {
    @Mock
    private PermissionsManager permissionsManager;

    @InjectMocks
    private PermissionCheckerImpl permissionChecker;

    @Test
    public void shouldCheckExistingDirectUsersPermissions() throws Exception {
        when(permissionsManager.exists(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        boolean hasPermission = permissionChecker.hasPermission("user123", "domain123", "instance123", "test");

        assertEquals(hasPermission, true);
        verify(permissionsManager).exists("user123", "domain123", "instance123", "test");
    }

    @Test
    public void shouldCheckExistingPublicPermissionsIfThereIsNoDirectUsersPermissions() throws Exception {
        when(permissionsManager.exists(eq("user123"), anyString(), anyString(), anyString())).thenReturn(false);
        when(permissionsManager.exists(eq("*"), anyString(), anyString(), anyString())).thenReturn(true);

        boolean hasPermission = permissionChecker.hasPermission("user123", "domain123", "instance123", "test");

        assertEquals(hasPermission, true);
        verify(permissionsManager).exists("user123", "domain123", "instance123", "test");
        verify(permissionsManager).exists("*", "domain123", "instance123", "test");
    }
}
