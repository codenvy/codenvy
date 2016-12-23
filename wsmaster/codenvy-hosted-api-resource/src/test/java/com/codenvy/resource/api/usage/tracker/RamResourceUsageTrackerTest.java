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

import com.codenvy.resource.api.RamResourceType;
import com.codenvy.resource.api.usage.tracker.RamResourceUsageTracker;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLimitsImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link RamResourceUsageTracker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RamResourceUsageTrackerTest {
    @Mock
    private Account          account;
    @Mock
    private Provider<WorkspaceManager> workspaceManagerProvider;
    @Mock
    private WorkspaceManager workspaceManager;
    @Mock
    private AccountManager   accountManager;

    @InjectMocks
    private RamResourceUsageTracker ramUsageTracker;

    @BeforeMethod
    public void setUp() throws Exception {
        when(workspaceManagerProvider.get()).thenReturn(workspaceManager);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Account was not found")
    public void shouldThrowNotFoundExceptionWhenAccountDoesNotExistOnGettingUsedRam() throws Exception {
        when(accountManager.getById(any())).thenThrow(new NotFoundException("Account was not found"));

        ramUsageTracker.getUsedResource("account123");
    }

    @Test
    public void shouldReturnEmptyOptionalWhenAccountHasOnlyStoppedWorkspaces() throws Exception {
        when(accountManager.getById(any())).thenReturn(account);
        when(account.getName()).thenReturn("testAccount");

        when(workspaceManager.getByNamespace(anyString()))
                .thenReturn(singletonList(createWorkspace(WorkspaceStatus.STOPPED, 1000, 500, 500)));

        Optional<ResourceImpl> usedRamOpt = ramUsageTracker.getUsedResource("account123");

        assertFalse(usedRamOpt.isPresent());
    }

    @Test
    public void shouldReturnUsedRamForGivenAccount() throws Exception {
        when(accountManager.getById(any())).thenReturn(account);
        when(account.getName()).thenReturn("testAccount");

        when(workspaceManager.getByNamespace(anyString()))
                .thenReturn(singletonList(createWorkspace(WorkspaceStatus.RUNNING, 1000, 500, 500)));

        Optional<ResourceImpl> usedRamOpt = ramUsageTracker.getUsedResource("account123");

        assertTrue(usedRamOpt.isPresent());
        ResourceImpl usedRam = usedRamOpt.get();
        assertEquals(usedRam.getType(), RamResourceType.ID);
        assertEquals(usedRam.getAmount(), 2000L);
        assertEquals(usedRam.getUnit(), RamResourceType.UNIT);
        verify(accountManager).getById(eq("account123"));
        verify(workspaceManager).getByNamespace(eq("testAccount"));
    }

    @Test
    public void shouldNotSumRamOfStoppedWorkspaceWhenGettingUsedRamForGivenAccount() throws Exception {
        when(accountManager.getById(any())).thenReturn(account);
        when(account.getName()).thenReturn("testAccount");

        when(workspaceManager.getByNamespace(anyString()))
                .thenReturn(singletonList(createWorkspace(WorkspaceStatus.STOPPED, 1000, 500, 500)));

        Optional<ResourceImpl> usedRamOpt = ramUsageTracker.getUsedResource("account123");

        assertFalse(usedRamOpt.isPresent());
        verify(accountManager).getById(eq("account123"));
        verify(workspaceManager).getByNamespace(eq("testAccount"));
    }

    /** Creates users workspace object based on the status and machines RAM. */
    public static WorkspaceImpl createWorkspace(WorkspaceStatus status, Integer... machineRams) {
        final List<MachineImpl> machines = new ArrayList<>(machineRams.length - 1);
        for (Integer machineRam : machineRams) {
            machines.add(createMachine(machineRam));
        }
        return WorkspaceImpl.builder()
                            .setRuntime(new WorkspaceRuntimeImpl(null, null, machines, null))
                            .setStatus(status)
                            .build();
    }

    private static MachineImpl createMachine(int memoryMb) {
        return MachineImpl.builder()
                          .setConfig(MachineConfigImpl.builder()
                                                      .setLimits(new MachineLimitsImpl(memoryMb))
                                                      .build())
                          .build();
    }

}
