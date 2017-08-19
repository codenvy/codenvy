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
package com.codenvy.plugin.jenkins.webhooks;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.codenvy.plugin.jenkins.webhooks.shared.JenkinsEventDto;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.factory.server.FactoryManager;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.eclipse.che.inject.ConfigurationProperties;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link JenkinsWebhookManager}.
 *
 * @author Igor Vinokur
 */
@Listeners(value = MockitoTestNGListener.class)
public class JenkinsWebhookManagerTest {

  private final Map<String, String> parameters = new HashMap<>();

  private JenkinsWebhookManager manager;
  private JenkinsEventDto jenkinsEvent;
  private JenkinsConnector jenkinsConnector;

  @Mock private FactoryManager factoryManager;

  @Mock private FactoryImpl factory;

  @BeforeMethod
  public void setup() throws Exception {
    parameters.clear();
    jenkinsConnector = mock(JenkinsConnector.class, new SelfReturningAnswer());
    JenkinsConnectorFactory jenkinsConnectorFactory = mock(JenkinsConnectorFactory.class);
    when(jenkinsConnectorFactory.create(anyString(), anyString())).thenReturn(jenkinsConnector);
    when(jenkinsConnector.getCommitId(anyInt())).thenReturn("commitId");
    when(jenkinsConnector.getFactoryId()).thenReturn("factoryId");
    UserManager userManager = mock(UserManager.class);
    User user = mock(User.class);
    when(userManager.getByName(anyString())).thenReturn(user);
    when(user.getId()).thenReturn("userId");
    Map<String, String> properties = new HashMap<>();
    properties.put("env.CODENVY_JENKINS_CONNECTOR_CONNECTORID_FACTORY_ID", "factoryId");
    properties.put("env.CODENVY_JENKINS_CONNECTOR_CONNECTORID_URL", "http://jenkins.url");
    properties.put("env.CODENVY_JENKINS_CONNECTOR_CONNECTORID_JOB_NAME", "jobName");
    ConfigurationProperties configurationProperties = mock(ConfigurationProperties.class);
    when(configurationProperties.getProperties(eq("env.CODENVY_JENKINS_CONNECTOR_.+")))
        .thenReturn(properties);
    WorkspaceConfigImpl workspace = mock(WorkspaceConfigImpl.class);
    when(factory.getWorkspace()).thenReturn(workspace);
    when(factory.getId()).thenReturn("factoryId");
    when(factoryManager.getByAttribute(anyInt(), eq(0), any())).thenReturn(singletonList(factory));
    when(factoryManager.getByAttribute(anyInt(), eq(30), any())).thenReturn(emptyList());
    when(factoryManager.getById("factoryId")).thenReturn(factory);
    when(factoryManager.saveFactory(factory)).thenReturn(factory);
    ProjectConfigImpl project = mock(ProjectConfigImpl.class);
    SourceStorageImpl source = mock(SourceStorageImpl.class);
    when(project.getSource()).thenReturn(source);
    when(workspace.getProjects()).thenReturn(singletonList(project));
    when(source.getType()).thenReturn("type");
    when(source.getLocation()).thenReturn("http://repository.git");
    when(source.getParameters()).thenReturn(parameters);

    jenkinsEvent =
        newDto(JenkinsEventDto.class)
            .withJenkinsUrl("http://jenkins.url")
            .withJobName("jobName")
            .withRepositoryUrl("http://repository.git");

    manager =
        new JenkinsWebhookManager(
            factoryManager, userManager, jenkinsConnectorFactory, "url/api", "username");
  }

  @Test
  public void shouldAddFailedBuildFactoryLink() throws Exception {
    //when
    manager.handleFailedJobEvent(jenkinsEvent);

    //then
    verify(jenkinsConnector).addFailedBuildFactoryLink(eq("url/f?id=factoryId"));
  }

  @Test
  public void shouldGenerateFailedBuildFactory() throws Exception {
    //given
    parameters.put("branch", "branch");
    when(factory.getName()).thenReturn("name");

    //when
    manager.handleFailedJobEvent(jenkinsEvent);

    //then
    verify(factoryManager).saveFactory(factory);
    verify(factory).setName(eq("name_commitId"));
    assertNull(parameters.get("branch"));
    assertEquals(parameters.get("commitId"), "commitId");
  }

  @Test
  public void shouldNotCreateNewFactoryIfFactoryWithCommitIdIsPresent() throws Exception {
    //given
    parameters.put("commitId", "commitId");

    //when
    manager.handleFailedJobEvent(jenkinsEvent);

    //then
    verify(factoryManager, never()).saveFactory(factory);
  }
}
