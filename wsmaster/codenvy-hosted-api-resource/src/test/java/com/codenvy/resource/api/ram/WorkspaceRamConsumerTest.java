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

import com.codenvy.resource.api.ResourceManager;
import com.codenvy.resource.spi.impl.ResourceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.environment.server.EnvironmentParser;
import org.eclipse.che.plugin.docker.compose.ComposeEnvironment;
import org.eclipse.che.plugin.docker.compose.ComposeServiceImpl;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Size;
import org.eclipse.che.plugin.docker.compose.yaml.ComposeEnvironmentParser;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WorkspaceRamConsumer}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceRamConsumerTest {
    private static final ObjectMapper YAML_PARSER = new ObjectMapper(new YAMLFactory());

    private static final int DEFAULT_SIZE_MB = 500;

    @Mock
    private ResourceManager  resourceManager;
    @Mock
    private WorkspaceManager workspaceManager;
    @Mock
    private AccountManager   accountManager;
    @Mock
    private MethodInvocation invocation;
    @Mock
    private AccountImpl      account;

    @Mock
    RecipeDownloader recipeDownloader;

    ComposeEnvironmentParser composeFileParser = new ComposeEnvironmentParser(recipeDownloader);

    EnvironmentParser environmentParser = new EnvironmentParser(singletonMap("compose", composeFileParser));

    @InjectMocks
    private WorkspaceRamConsumer ramConsumer;

    @BeforeMethod
    public void setUp() throws Exception {
        ramConsumer.environmentParser = environmentParser;
        ramConsumer.defaultMachineMemorySizeMB = DEFAULT_SIZE_MB;
    }

    @Test
    public void shouldReserveRamOnStartingWorkspaceById() throws Throwable {
        when(invocation.getArguments()).thenReturn(new Object[] {"workspace123", "dev-env", false});
        final WorkspaceImpl testWorkspace = createWorkspace("testAccount", "dev-env", "default", 700, 300);
        when(workspaceManager.getWorkspace(any())).thenReturn(testWorkspace);
        when(accountManager.getByName(any())).thenReturn(account);
        when(account.getId()).thenReturn("account123");

        ramConsumer.invoke(invocation);

        verify(workspaceManager).getWorkspace(eq("workspace123"));
        verify(accountManager).getByName(eq("testAccount"));
        verify(resourceManager).reserveResources(eq("account123"), eq(singletonList(new ResourceImpl(RamResourceType.ID,
                                                                                                     1000,
                                                                                                     RamResourceType.UNIT))), any());
    }

    @Test
    public void shouldReserveRamFromDefaultEvnIfItIsNotSpecifiedOnStartingWorkspaceById() throws Throwable {
        when(invocation.getArguments()).thenReturn(new Object[] {"workspace123", null, false});
        final WorkspaceImpl testWorkspace = createWorkspace("testAccount", "default", 700, 300);
        when(workspaceManager.getWorkspace(any())).thenReturn(testWorkspace);
        when(accountManager.getByName(any())).thenReturn(account);
        when(account.getId()).thenReturn("account123");

        ramConsumer.invoke(invocation);

        verify(workspaceManager).getWorkspace(eq("workspace123"));
        verify(accountManager).getByName(eq("testAccount"));
        verify(resourceManager).reserveResources(eq("account123"), eq(singletonList(new ResourceImpl(RamResourceType.ID,
                                                                                                     1000,
                                                                                                     RamResourceType.UNIT))), any());
    }

    @Test
    public void shouldReserveDefaultMachineMemoryLimitIfItIsNotSpecifiedByConfigRamOnStartingWorkspaceById() throws Throwable {
        when(invocation.getArguments()).thenReturn(new Object[] {"workspace123", "dev-env", false});
        final WorkspaceImpl testWorkspace = createWorkspace("testAccount", "dev-env", "default", null, 300);
        when(workspaceManager.getWorkspace(any())).thenReturn(testWorkspace);
        when(accountManager.getByName(any())).thenReturn(account);
        when(account.getId()).thenReturn("account123");

        ramConsumer.invoke(invocation);

        verify(workspaceManager).getWorkspace(eq("workspace123"));
        verify(accountManager).getByName(eq("testAccount"));
        verify(resourceManager).reserveResources(eq("account123"), eq(singletonList(new ResourceImpl(RamResourceType.ID,
                                                                                                     DEFAULT_SIZE_MB + 300,
                                                                                                     RamResourceType.UNIT))), any());
    }

    @Test
    public void shouldReserveDefaultMachineMemoryLimitIfItIsNotSpecifiedByConfigRamOnStartingWorkspaceFromConfig() throws Throwable {
        final WorkspaceConfigImpl config = createConfig("default", "default", 700, null);
        when(invocation.getArguments()).thenReturn(new Object[] {config, "testAccount"});
        when(accountManager.getByName(any())).thenReturn(account);
        when(account.getId()).thenReturn("account123");

        ramConsumer.invoke(invocation);

        verify(accountManager).getByName(eq("testAccount"));
        verify(resourceManager).reserveResources(eq("account123"), eq(singletonList(new ResourceImpl(RamResourceType.ID,
                                                                                                     DEFAULT_SIZE_MB + 700,
                                                                                                     RamResourceType.UNIT))), any());
    }

    @Test
    public void shouldReserveRamFromDefaultEvnOnStartingWorkspaceFromConfig() throws Throwable {
        final WorkspaceConfigImpl config = createConfig("default", "default", 700, 300);
        when(invocation.getArguments()).thenReturn(new Object[] {config, "testAccount"});
        when(accountManager.getByName(any())).thenReturn(account);
        when(account.getId()).thenReturn("account123");

        ramConsumer.invoke(invocation);

        verify(accountManager).getByName(eq("testAccount"));
        verify(resourceManager).reserveResources(eq("account123"), eq(singletonList(new ResourceImpl(RamResourceType.ID,
                                                                                                     1000,
                                                                                                     RamResourceType.UNIT))), any());
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

    public static WorkspaceImpl createWorkspace(String namespace, String envName, Integer... machineRamsMb) throws Exception {
        return createWorkspace(namespace, envName, envName, machineRamsMb);
    }

    /** Creates users workspace object based on the owner and machines RAM. */
    public static WorkspaceImpl createWorkspace(String namespace, String envName, String defaultEnvName, Integer... machineRamsMb)
            throws Exception {
        return WorkspaceImpl.builder()
                            .setConfig(createConfig(envName, defaultEnvName, machineRamsMb))
                            .setAccount(new AccountImpl("id", namespace, "test"))
                            .build();
    }

    public static WorkspaceConfigImpl createConfig(String envName, String defaultEnvName, Integer... machineRamsMb) throws Exception {
        Map<String, ExtendedMachineImpl> machines = new HashMap<>();
        HashMap<String, ComposeServiceImpl> services = new HashMap<>(machineRamsMb.length);
        for (int i = 0; i < machineRamsMb.length; i++) {
            services.put("machine" + i, createService());
            // null is allowed to reproduce situation with default RAM size
            if (machineRamsMb[i] != null) {
                machines.put("machine" + i, new ExtendedMachineImpl(null,
                                                                    null,
                                                                    new HashMap<>(singletonMap("memoryLimitBytes",
                                                                                               Long.toString(Size.parseSize(
                                                                                                       machineRamsMb[i] + "mb"))))));
            }
        }
        ComposeEnvironment composeEnvironment = new ComposeEnvironment();
        composeEnvironment.setServices(services);
        String yaml = YAML_PARSER.writeValueAsString(composeEnvironment);
        EnvironmentRecipeImpl recipe = new EnvironmentRecipeImpl("compose", "application/x-yaml", yaml, null);
        return WorkspaceConfigImpl.builder()
                                  .setName(NameGenerator.generate("workspace", 2))
                                  .setEnvironments(singletonMap(envName,
                                                                new EnvironmentImpl(recipe,
                                                                                    machines)))
                                  .setDefaultEnv(defaultEnvName)
                                  .build();
    }

    private static ComposeServiceImpl createService() {
        ComposeServiceImpl service = new ComposeServiceImpl();
        service.setImage("image");
        return service;
    }
}
