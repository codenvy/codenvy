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
package com.codenvy.machine;

import com.codenvy.machine.authentication.server.MachineTokenRegistry;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.util.RecipeRetriever;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerState;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.InspectContainerParams;
import org.eclipse.che.plugin.docker.machine.DockerAgentConfigApplier;
import org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator;
import org.eclipse.che.plugin.docker.machine.DockerInstanceProvider;
import org.eclipse.che.plugin.docker.machine.DockerInstanceStopDetector;
import org.eclipse.che.plugin.docker.machine.DockerMachineFactory;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_KEY;
import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_VALUE;
import static java.util.Arrays.asList;
import static org.eclipse.che.plugin.docker.machine.DockerInstanceProvider.DOCKER_FILE_TYPE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Listeners(MockitoTestNGListener.class)
public class HostedDockerInstanceProviderTest {

    @Mock
    private DockerConnector                               dockerConnector;
    @Mock
    private DockerConnectorConfiguration                  dockerConnectorConfiguration;
    @Mock
    private DockerMachineFactory                          dockerMachineFactory;
    @Mock
    private DockerInstanceStopDetector                    dockerInstanceStopDetector;
    @Mock
    private DockerContainerNameGenerator                  containerNameGenerator;
    @Mock
    private DockerNode                                    dockerNode;
    @Mock
    private WorkspaceFolderPathProvider                   workspaceFolderPathProvider;
    @Mock
    private UserSpecificDockerRegistryCredentialsProvider credentialsReader;
    @Mock
    private ContainerInfo                                 containerInfo;
    @Mock
    private ContainerState                                containerState;
    @Mock
    private RecipeRetriever                               recipeRetriever;
    @Mock
    private MachineTokenRegistry                          machineTokenRegistry;
    @Mock
    private DockerAgentConfigApplier                      configApplier;

    private DockerInstanceProvider dockerInstanceProvider;

    private static final String  PROJECT_FOLDER_PATH    = "/projects";
    private static final String  CONTAINER_ID           = "containerId";
    private static final String  WORKSPACE_ID           = "wsId";
    private static final String  MACHINE_ID             = "machineId";
    private static final String  MACHINE_NAME           = "machineName";
    private static final String  USER_TOKEN             = "userToken";
    private static final String  USER_NAME              = "user";
    private static final int     MEMORY_LIMIT_MB        = 64;
    private static final boolean SNAPSHOT_USE_REGISTRY  = true;
    private static final int     MEMORY_SWAP_MULTIPLIER = 0;

    @BeforeMethod
    public void setUp() throws Exception {
        when(dockerConnectorConfiguration.getDockerHostIp()).thenReturn("123.123.123.123");

        dockerInstanceProvider = spy(new DockerInstanceProvider(dockerConnector,
                                                                dockerConnectorConfiguration,
                                                                credentialsReader,
                                                                dockerMachineFactory,
                                                                dockerInstanceStopDetector,
                                                                containerNameGenerator,
                                                                recipeRetriever,
                                                                configApplier,
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                null,
                                                                workspaceFolderPathProvider,
                                                                PROJECT_FOLDER_PATH,
                                                                false,
                                                                false,
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                SNAPSHOT_USE_REGISTRY,
                                                                MEMORY_SWAP_MULTIPLIER));

        EnvironmentContext envCont = new EnvironmentContext();
        envCont.setSubject(new SubjectImpl(USER_NAME, "userId", USER_TOKEN, false));
        EnvironmentContext.setCurrent(envCont);


        when(recipeRetriever.getRecipe(any(MachineConfig.class)))
                .thenReturn(new RecipeImpl().withType(DOCKER_FILE_TYPE).withScript("FROM codenvy"));

        when(dockerMachineFactory.createNode(anyString(), anyString())).thenReturn(dockerNode);
        when(dockerConnector.createContainer(any(CreateContainerParams.class)))
                .thenReturn(new ContainerCreated(CONTAINER_ID, new String[0]));
        when(dockerConnector.inspectContainer(any(InspectContainerParams.class))).thenReturn(containerInfo);
        when(containerInfo.getState()).thenReturn(containerState);
        when(containerState.isRunning()).thenReturn(false);
    }

    @Test
    public void shouldAddMaintenanceConstraintWhenBuildImage() throws Exception {
        dockerInstanceProvider = new HostedDockerInstanceProvider(dockerConnector,
                                                                  dockerConnectorConfiguration,
                                                                  credentialsReader,
                                                                  dockerMachineFactory,
                                                                  dockerInstanceStopDetector,
                                                                  containerNameGenerator,
                                                                  recipeRetriever,
                                                                  Collections.emptySet(),
                                                                  Collections.emptySet(),
                                                                  Collections.emptySet(),
                                                                  Collections.emptySet(),
                                                                  null,
                                                                  workspaceFolderPathProvider,
                                                                  PROJECT_FOLDER_PATH,
                                                                  false,
                                                                  false,
                                                                  Collections.emptySet(),
                                                                  Collections.emptySet(),
                                                                  SNAPSHOT_USE_REGISTRY,
                                                                  MEMORY_SWAP_MULTIPLIER,
                                                                  machineTokenRegistry,
                                                                  configApplier);

        createInstanceFromRecipe(getMachineBuilder().setConfig(getMachineConfigBuilder().setDev(true)
                                                                                        .build())
                                                    .build());
        ArgumentCaptor<BuildImageParams> argumentCaptor = ArgumentCaptor.forClass(BuildImageParams.class);
        verify(dockerConnector).buildImage(argumentCaptor.capture(), anyObject());
        assertNotNull(argumentCaptor.getValue().getBuildArgs().get(MAINTENANCE_CONSTRAINT_KEY));
        assertEquals(argumentCaptor.getValue().getBuildArgs().get(MAINTENANCE_CONSTRAINT_KEY), MAINTENANCE_CONSTRAINT_VALUE);
    }

    private void createInstanceFromRecipe(Machine machine) throws Exception {
        dockerInstanceProvider.createInstance(machine,
                                              LineConsumer.DEV_NULL);
    }

    private MachineImpl.MachineImplBuilder getMachineBuilder() {
        return MachineImpl.builder().fromMachine(new MachineImpl(getMachineConfigBuilder().build(),
                                                                 MACHINE_ID,
                                                                 WORKSPACE_ID,
                                                                 "envName",
                                                                 "userId",
                                                                 MachineStatus.CREATING,
                                                                 null));
    }

    private MachineConfigImpl.MachineConfigImplBuilder getMachineConfigBuilder() {
        return MachineConfigImpl.builder().fromConfig(new MachineConfigImpl(false,
                                                                            MACHINE_NAME,
                                                                            "machineType",
                                                                            new MachineSourceImpl(DOCKER_FILE_TYPE)
                                                                                    .setContent("FROM codenvy"),
                                                                            new LimitsImpl(MEMORY_LIMIT_MB),
                                                                            asList(new ServerConfImpl("ref1",
                                                                                                      "8080",
                                                                                                      "https",
                                                                                                      null),
                                                                                   new ServerConfImpl("ref2",
                                                                                                      "9090/udp",
                                                                                                      "someprotocol",
                                                                                                      null)),
                                                                            Collections.singletonMap("key1", "value1"),
                                                                            Collections.emptyList()));
    }

}
