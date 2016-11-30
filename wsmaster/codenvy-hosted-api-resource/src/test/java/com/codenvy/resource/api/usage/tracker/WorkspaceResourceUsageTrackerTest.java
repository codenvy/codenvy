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
package com.codenvy.resource.api.usage.tracker;

import com.codenvy.resource.api.WorkspaceResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link WorkspaceResourceUsageTracker}
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceResourceUsageTrackerTest {
    @Mock
    private Provider<WorkspaceManager> workspaceManagerProvider;
    @Mock
    private WorkspaceManager           workspaceManager;
    @Mock
    private AccountManager             accountManager;
    @Mock
    private Account                    account;

    @InjectMocks
    private WorkspaceResourceUsageTracker workspaceResourceUsageTracker;

    @BeforeMethod
    public void setUp() throws Exception {
        when(workspaceManagerProvider.get()).thenReturn(workspaceManager);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Account was not found")
    public void shouldThrowNotFoundExceptionWhenAccountDoesNotExistOnGettingUsedWorkspaces() throws Exception {
        when(accountManager.getById(any())).thenThrow(new NotFoundException("Account was not found"));

        workspaceResourceUsageTracker.getUsedResource("account123");
    }

    @Test
    public void shouldReturnEmptyOptionalWhenAccountDoesNotUseWorkspaces() throws Exception {
        when(accountManager.getById(any())).thenReturn(account);
        when(account.getName()).thenReturn("testAccount");

        when(workspaceManager.getByNamespace(anyString())).thenReturn(Collections.emptyList());

        Optional<ResourceImpl> usedWorkspacesOpt = workspaceResourceUsageTracker.getUsedResource("account123");

        assertFalse(usedWorkspacesOpt.isPresent());
    }

    @Test
    public void shouldReturnUsedWorkspacesForGivenAccount() throws Exception {
        when(accountManager.getById(any())).thenReturn(account);
        when(account.getName()).thenReturn("testAccount");

        when(workspaceManager.getByNamespace(anyString()))
                .thenReturn(Arrays.asList(new WorkspaceImpl(), new WorkspaceImpl(), new WorkspaceImpl()));

        Optional<ResourceImpl> usedWorkspacesOpt = workspaceResourceUsageTracker.getUsedResource("account123");

        assertTrue(usedWorkspacesOpt.isPresent());
        ResourceImpl usedWorkspaces = usedWorkspacesOpt.get();
        assertEquals(usedWorkspaces.getType(), WorkspaceResourceType.ID);
        assertEquals(usedWorkspaces.getAmount(), 3);
        assertEquals(usedWorkspaces.getUnit(), WorkspaceResourceType.UNIT);
        verify(accountManager).getById(eq("account123"));
        verify(workspaceManager).getByNamespace(eq("testAccount"));
    }
}
