/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.machine;

import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_KEY;
import static com.codenvy.machine.MaintenanceConstraintProvider.MAINTENANCE_CONSTRAINT_VALUE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.codenvy.machine.authentication.server.MachineTokenRegistry;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.util.JsonRpcEndpointToMachineNameHolder;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.machine.server.util.RecipeRetriever;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerConnectorProvider;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.exception.ContainerNotFoundException;
import org.eclipse.che.plugin.docker.client.exception.DockerException;
import org.eclipse.che.plugin.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerState;
import org.eclipse.che.plugin.docker.client.json.ImageConfig;
import org.eclipse.che.plugin.docker.client.json.ImageInfo;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.InspectContainerParams;
import org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator;
import org.eclipse.che.plugin.docker.machine.DockerInstanceStopDetector;
import org.eclipse.che.plugin.docker.machine.DockerMachineFactory;
import org.eclipse.che.plugin.docker.machine.MachineProviderImpl;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class HostedMachineProviderImplTest {

  @Mock private DockerConnectorProvider dockerConnectorProviderMock;
  @Mock private DockerConnector dockerConnector;
  @Mock private DockerConnectorConfiguration dockerConnectorConfiguration;
  @Mock private DockerMachineFactory dockerMachineFactory;
  @Mock private DockerInstanceStopDetector dockerInstanceStopDetector;
  @Mock private DockerContainerNameGenerator containerNameGenerator;
  @Mock private RequestTransmitter requestTransmitter;
  @Mock private JsonRpcEndpointToMachineNameHolder endpointIdsHolder;
  @Mock private DockerNode dockerNode;
  @Mock private UserSpecificDockerRegistryCredentialsProvider credentialsReader;
  @Mock private ContainerInfo containerInfo;
  @Mock private ContainerState containerState;
  @Mock private ImageInfo imageInfo;
  @Mock private ImageConfig imageConfig;
  @Mock private RecipeRetriever recipeRetriever;
  @Mock private MachineTokenRegistry machineTokenRegistry;

  private WindowsPathEscaper windowsPathEscaper = new WindowsPathEscaper();

  private MachineProviderImpl provider;

  private static final String CONTAINER_ID = "containerId";
  private static final String WORKSPACE_ID = "wsId";
  private static final String MACHINE_NAME = "machineName";
  private static final String USER_TOKEN = "userToken";
  private static final String USER_NAME = "user";
  private static final boolean SNAPSHOT_USE_REGISTRY = true;
  private static final int MEMORY_SWAP_MULTIPLIER = 0;

  @BeforeMethod
  public void setUp() throws Exception {
    // let tests pass faster
    HostedMachineProviderImpl.SWARM_WAIT_BEFORE_REPEAT_WORKAROUND_TIME_MS = 10;

    when(dockerConnectorProviderMock.get()).thenReturn(dockerConnector);
    when(dockerConnectorConfiguration.getDockerHostIp()).thenReturn("123.123.123.123");

    EnvironmentContext envCont = new EnvironmentContext();
    envCont.setSubject(new SubjectImpl(USER_NAME, "userId", USER_TOKEN, false));
    EnvironmentContext.setCurrent(envCont);

    when(dockerMachineFactory.createNode(anyString(), anyString())).thenReturn(dockerNode);
    when(dockerConnector.createContainer(any(CreateContainerParams.class)))
        .thenReturn(new ContainerCreated(CONTAINER_ID, new String[0]));
    when(dockerConnector.inspectContainer(any(InspectContainerParams.class)))
        .thenReturn(containerInfo);
    when(containerInfo.getState()).thenReturn(containerState);
    when(dockerConnector.inspectContainer(anyString())).thenReturn(containerInfo);
    when(containerState.getStatus()).thenReturn("running");
    when(dockerConnector.inspectImage(anyString())).thenReturn(imageInfo);
    when(imageInfo.getConfig()).thenReturn(imageConfig);
    when(imageConfig.getCmd()).thenReturn(new String[] {"tail", "-f", "/dev/null"});

    provider =
        new HostedMachineProviderImpl(
            dockerConnectorProviderMock,
            credentialsReader,
            dockerMachineFactory,
            dockerInstanceStopDetector,
            windowsPathEscaper,
            requestTransmitter,
            endpointIdsHolder,
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet(),
            false,
            false,
            -1,
            emptySet(),
            emptySet(),
            SNAPSHOT_USE_REGISTRY,
            MEMORY_SWAP_MULTIPLIER,
            machineTokenRegistry,
            emptySet(),
            null,
            null,
            null,
            0,
            0,
            emptySet(),
            null,
            emptyMap());
  }

  @Test
  public void shouldAddMaintenanceConstraintWhenBuildImage() throws Exception {
    // when
    createInstanceFromRecipe();

    // then
    ArgumentCaptor<BuildImageParams> argumentCaptor =
        ArgumentCaptor.forClass(BuildImageParams.class);
    verify(dockerConnector).buildImage(argumentCaptor.capture(), anyObject());
    assertNotNull(argumentCaptor.getValue().getBuildArgs().get(MAINTENANCE_CONSTRAINT_KEY));
    assertEquals(
        argumentCaptor.getValue().getBuildArgs().get(MAINTENANCE_CONSTRAINT_KEY),
        MAINTENANCE_CONSTRAINT_VALUE);
  }

  @Test
  public void shouldAddCpuConsumptionLimitsWhenBuildImage() throws Exception {
    // given
    final String cpusetCpus = "2,4";
    final Long cpuPeriod = 10000L;
    final Long cpuQuota = 7500L;

    provider =
        new HostedMachineProviderImpl(
            dockerConnectorProviderMock,
            credentialsReader,
            dockerMachineFactory,
            dockerInstanceStopDetector,
            windowsPathEscaper,
            requestTransmitter,
            endpointIdsHolder,
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet(),
            false,
            false,
            -1,
            emptySet(),
            emptySet(),
            SNAPSHOT_USE_REGISTRY,
            MEMORY_SWAP_MULTIPLIER,
            machineTokenRegistry,
            emptySet(),
            null,
            null,
            cpusetCpus,
            cpuPeriod,
            cpuQuota,
            emptySet(),
            null,
            emptyMap());

    // when
    createInstanceFromRecipe();

    // then
    ArgumentCaptor<BuildImageParams> argumentCaptor =
        ArgumentCaptor.forClass(BuildImageParams.class);
    verify(dockerConnector).buildImage(argumentCaptor.capture(), anyObject());
    assertEquals(argumentCaptor.getValue().getCpusetCpus(), cpusetCpus);
    assertEquals(argumentCaptor.getValue().getCpuPeriod(), cpuPeriod);
    assertEquals(argumentCaptor.getValue().getCpuQuota(), cpuQuota);
  }

  @Test
  public void shouldNotRepeatImageInspectionOnCheckEntrypointCmdIfInspectionSucceeds()
      throws Exception {
    // when
    createInstanceFromRecipe();

    // then
    verify(dockerConnector).inspectImage(anyString());
  }

  @Test
  public void shouldWorkaroundImageNotFoundIssueInSwarmOnCheckEntrypointCmd() throws Exception {
    // given
    when(dockerConnector.inspectImage(anyString()))
        .thenThrow(new ImageNotFoundException("test exception"))
        .thenReturn(imageInfo);

    // when
    createInstanceFromRecipe();

    // then
    verify(dockerConnector, times(2)).inspectImage(anyString());
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldNotRepeatImageInspectionOnCheckEntrypointCmdIfInspectionExceptionDiffers()
      throws Exception {
    // given
    when(dockerConnector.inspectImage(anyString()))
        .thenThrow(new DockerException("test exception", 500))
        .thenReturn(imageInfo);

    // when
    try {
      createInstanceFromRecipe();
    } finally {
      // then
      verify(dockerConnector).inspectImage(anyString());
    }
  }

  @Test
  public void shouldNotRepeatContainerInspectionOnCheckContainerStatusIfInspectionSucceeds()
      throws Exception {
    // when
    createInstanceFromRecipe();

    // then
    verify(dockerConnector).inspectContainer(anyString());
  }

  @Test
  public void shouldWorkaroundContainerNotFoundIssueInSwarmOnCheckContainerStatus()
      throws Exception {
    // given
    when(dockerConnector.inspectContainer(anyString()))
        .thenThrow(new ContainerNotFoundException("test exception"))
        .thenReturn(containerInfo);

    // when
    createInstanceFromRecipe();

    // then
    verify(dockerConnector, times(2)).inspectContainer(anyString());
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldNotRepeatContainerInspectionOnCheckContainerStatusIfInspectionExceptionDiffers()
      throws Exception {
    // given
    when(dockerConnector.inspectContainer(anyString()))
        .thenThrow(new DockerException("test exception", 500))
        .thenReturn(containerInfo);

    // when
    try {
      createInstanceFromRecipe();
    } finally {
      // then
      verify(dockerConnector).inspectContainer(anyString());
    }
  }

  public CheServiceImpl createService() {
    CheServiceImpl service = new CheServiceImpl();
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

  private void createInstanceFromRecipe() throws Exception {
    provider.startService(
        USER_NAME,
        WORKSPACE_ID,
        "env",
        MACHINE_NAME,
        true,
        "net",
        createService(),
        LineConsumer.DEV_NULL);
  }
}
