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
package com.codenvy.resource.api.ram;

import com.codenvy.api.workspace.EnvironmentRamCalculator;
import com.codenvy.resource.api.usage.ResourceUsageManager;
import com.codenvy.resource.api.usage.ResourcesLocks;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.lang.concurrent.CloseableLock;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link StartWorkspaceResourcesLocker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class StartWorkspaceResourcesLockerTest {
    @Mock
    private ResourceUsageManager     resourceUsageManager;
    @Mock
    private WorkspaceManager         workspaceManager;
    @Mock
    private AccountManager           accountManager;
    @Mock
    private ResourcesLocks           resourcesLocks;
    @Mock
    private EnvironmentRamCalculator ramCalculator;

    @Mock
    private MethodInvocation invocation;
    @Mock
    private Account          account;

    @Mock
    private WorkspaceImpl                workspace;
    @Mock
    private WorkspaceConfigImpl          workspaceConfig;
    @Mock
    private Map<String, EnvironmentImpl> environments;
    @Mock
    private EnvironmentImpl              environment;


    @Mock
    private CloseableLock lock;

    @InjectMocks
    private StartWorkspaceResourcesLocker resourcesLocker;

    @BeforeMethod
    public void setUp() throws Exception {
        when(workspace.getNamespace()).thenReturn("testAccount");
        when(workspace.getConfig()).thenReturn(workspaceConfig);

        when(workspaceConfig.getEnvironments()).thenReturn(environments);
        when(workspaceConfig.getDefaultEnv()).thenReturn("default");

        when(environments.get(anyString())).thenReturn(environment);

        when(workspaceManager.getWorkspace(any())).thenReturn(workspace);

        when(accountManager.getByName(any())).thenReturn(account);
        when(account.getId()).thenReturn("account123");

        when(ramCalculator.calculate(anyObject())).thenReturn(1000L);

        when(resourcesLocks.acquiresLock(anyString())).thenReturn(lock);
    }

    @Test
    public void shouldReserveRamOnStartingWorkspaceById() throws Throwable {
        when(invocation.getArguments()).thenReturn(new Object[] {"workspace123", "dev-env", false});

        resourcesLocker.invoke(invocation);

        verify(workspaceManager).getWorkspace("workspace123");
        verify(environments).get("dev-env");
        verify(accountManager).getByName("testAccount");
        verify(resourcesLocks).acquiresLock("account123");
        verify(lock).close();
        verify(ramCalculator).calculate(environment);
        verify(resourceUsageManager).checkResourcesAvailability("account123", singletonList(new ResourceImpl(RamResourceType.ID,
                                                                                                             1000,
                                                                                                             RamResourceType.UNIT)));
    }

    @Test
    public void shouldReserveRamFromDefaultEvnIfItIsNotSpecifiedOnStartingWorkspaceById() throws Throwable {
        when(invocation.getArguments()).thenReturn(new Object[] {"workspace123", null, false});

        resourcesLocker.invoke(invocation);

        verify(workspaceManager).getWorkspace("workspace123");
        verify(environments).get("default");
        verify(accountManager).getByName("testAccount");
        verify(resourcesLocks).acquiresLock("account123");
        verify(lock).close();
        verify(ramCalculator).calculate(environment);
        verify(resourceUsageManager).checkResourcesAvailability("account123", singletonList(new ResourceImpl(RamResourceType.ID,
                                                                                                             1000,
                                                                                                             RamResourceType.UNIT)));
    }

    @Test
    public void shouldReserveRamFromDefaultEvnOnStartingWorkspaceFromConfig() throws Throwable {
        when(invocation.getArguments()).thenReturn(new Object[] {workspaceConfig, "testAccount"});
        when(accountManager.getByName(any())).thenReturn(account);
        when(account.getId()).thenReturn("account123");

        resourcesLocker.invoke(invocation);

        verify(environments).get("default");
        verify(accountManager).getByName("testAccount");
        verify(resourcesLocks).acquiresLock("account123");
        verify(lock).close();
        verify(ramCalculator).calculate(environment);
        verify(resourceUsageManager).checkResourcesAvailability("account123", singletonList(new ResourceImpl(RamResourceType.ID,
                                                                                                             1000,
                                                                                                             RamResourceType.UNIT)));
    }

    @Test(dataProvider = "interceptedMethods")
    public void shouldTestRequiredMethodsExistence(Object[] methodDescriptor) throws Exception {
        //
        final Method method = getServiceMethod(methodDescriptor);
        when(invocation.getMethod()).thenReturn(method);
        // preparing invocation arguments which are actually the same size as method parameters
        // settings the last argument value to fake account identifier
        final Object[] invocationArgs = new Object[method.getParameterCount()];
        invocationArgs[invocationArgs.length - 1] = "not default account id";
        when(invocation.getArguments()).thenReturn(invocationArgs);
    }

    @DataProvider(name = "interceptedMethods")
    private Object[][] interceptedMethodsProvider() {
        return new Object[][] {
                {new Object[] {"startWorkspace", String.class, String.class, Boolean.class}},
                {new Object[] {"startWorkspace", WorkspaceConfig.class, String.class, boolean.class}}
        };
    }

    /**
     * Gets a {@link WorkspaceManager} method based on data provided by {@link #interceptedMethodsProvider()
     */
    private Method getServiceMethod(Object[] methodDescription) throws NoSuchMethodException {
        final Class<?>[] methodParams = new Class<?>[methodDescription.length - 1];
        for (int i = 1; i < methodDescription.length; i++) {
            methodParams[i - 1] = (Class<?>)methodDescription[i];
        }
        return WorkspaceManager.class.getMethod(methodDescription[0].toString(), methodParams);
    }
}
