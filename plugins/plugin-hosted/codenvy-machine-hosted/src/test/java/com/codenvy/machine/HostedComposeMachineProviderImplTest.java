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

import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.environment.server.compose.model.ComposeServiceImpl;
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
import org.eclipse.che.plugin.docker.machine.ComposeMachineProviderImpl;
import org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator;
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

import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_KEY;
import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_VALUE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Listeners(MockitoTestNGListener.class)
public class HostedComposeMachineProviderImplTest {

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

    private ComposeMachineProviderImpl provider;

    private static final String  PROJECT_FOLDER_PATH    = "/projects";
    private static final String  CONTAINER_ID           = "containerId";
    private static final String  WORKSPACE_ID           = "wsId";
    private static final String  MACHINE_ID             = "machineId";
    private static final String  MACHINE_NAME           = "machineName";
    private static final String  USER_TOKEN             = "userToken";
    private static final String  USER_NAME              = "user";
    private static final boolean SNAPSHOT_USE_REGISTRY  = true;
    private static final int     MEMORY_SWAP_MULTIPLIER = 0;

    @BeforeMethod
    public void setUp() throws Exception {
        when(dockerConnectorConfiguration.getDockerHostIp()).thenReturn("123.123.123.123");

        EnvironmentContext envCont = new EnvironmentContext();
        envCont.setSubject(new SubjectImpl(USER_NAME, "userId", USER_TOKEN, false));
        EnvironmentContext.setCurrent(envCont);

        when(dockerMachineFactory.createNode(anyString(), anyString())).thenReturn(dockerNode);
        when(dockerConnector.createContainer(any(CreateContainerParams.class)))
                .thenReturn(new ContainerCreated(CONTAINER_ID, new String[0]));
        when(dockerConnector.inspectContainer(any(InspectContainerParams.class))).thenReturn(containerInfo);
        when(containerInfo.getState()).thenReturn(containerState);
        when(containerState.isRunning()).thenReturn(false);
    }

    @Test
    public void shouldAddMaintenanceConstraintWhenBuildImage() throws Exception {
        provider = new HostedComposeMachineProviderImpl(dockerConnector,
                                                        dockerConnectorConfiguration,
                                                        credentialsReader,
                                                        dockerMachineFactory,
                                                        dockerInstanceStopDetector,
                                                        containerNameGenerator,
                                                        emptySet(),
                                                        emptySet(),
                                                        emptySet(),
                                                        emptySet(),
                                                        null,
                                                        workspaceFolderPathProvider,
                                                        PROJECT_FOLDER_PATH,
                                                        false,
                                                        false,
                                                        emptySet(),
                                                        emptySet(),
                                                        SNAPSHOT_USE_REGISTRY,
                                                        MEMORY_SWAP_MULTIPLIER,
                                                        machineTokenRegistry,
                                                        emptySet());

        createInstanceFromRecipe(true);
        ArgumentCaptor<BuildImageParams> argumentCaptor = ArgumentCaptor.forClass(BuildImageParams.class);
        verify(dockerConnector).buildImage(argumentCaptor.capture(), anyObject());
        assertNotNull(argumentCaptor.getValue().getBuildArgs().get(MAINTENANCE_CONSTRAINT_KEY));
        assertEquals(argumentCaptor.getValue().getBuildArgs().get(MAINTENANCE_CONSTRAINT_KEY), MAINTENANCE_CONSTRAINT_VALUE);
    }

    public ComposeServiceImpl createService() {
        ComposeServiceImpl service = new ComposeServiceImpl();
        service.setImage("image");
        service.setCommand(asList("some", "command"));
        service.setContainerName("cont_name");
        service.setDependsOn(asList("dep1", "dep2"));
        service.setEntrypoint(asList("entry", "point"));
        service.setExpose(asList("1010", "1111"));
        service.setEnvironment(singletonMap("some", "var"));
        service.setLabels(singletonMap("some", "label"));
        service.setLinks(asList("link1", "link2:alias"));
        service.setMemLimit(1000000000L);
        service.setPorts(asList("port1", "port2"));
        service.setVolumes(asList("vol1", "vol2"));
        service.setVolumesFrom(asList("from1", "from2"));
        return service;
    }

    private void createInstanceFromRecipe(boolean isDev) throws Exception {
        provider.startService(USER_NAME,
                              WORKSPACE_ID,
                              "env",
                              MACHINE_ID,
                              MACHINE_NAME,
                              isDev,
                              "net",
                              createService(),
                              LineConsumer.DEV_NULL);
    }
}
