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
package com.codenvy.plugin.webhooks;

import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codenvy.plugin.webhooks.github.GitHubWebhookService;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.commons.test.servlet.MockServletInputStream;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.inject.ConfigurationProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for GitHubWebhookService
 *
 * @author Stephane Tournie
 */
@RunWith(MockitoJUnitRunner.class)
public class TestGitHubWebhookService {

  private static final String REQUEST_HEADER_GITHUB_EVENT = "X-GitHub-Event";
  private static final String FAKE_USER_ID = "TEST_USER_ID";

  private enum Service {
    GITHUB,
    VSTS
  }

  private GitHubWebhookService fakeGitHubWebhookService;

  @Before
  public void setUp() throws Exception {
    // Prepare webhook
    ConfigurationProperties configurationProperties = mock(ConfigurationProperties.class);
    Map<String, String> properties = new HashMap<>();
    properties.put(
        "env.CODENVY_GITHUB_WEBHOOK_WEBHOOK1_REPOSITORY_URL",
        "https://github.com/codenvy-demos/dashboard");
    properties.put("env.CODENVY_GITHUB_WEBHOOK_WEBHOOK1_FACTORY1_ID", "fakeFactoryId");
    when(configurationProperties.getProperties(eq("env.CODENVY_GITHUB_WEBHOOK_.+")))
        .thenReturn(properties);

    // Prepare authConnection
    Token fakeToken = DtoFactory.newDto(Token.class).withValue("fakeToken");
    AuthConnection mockAuthConnection = mock(AuthConnection.class);
    when(mockAuthConnection.authenticateUser("somebody@somemail.com", "somepwd"))
        .thenReturn(fakeToken);

    // Prepare userConnection
    UserConnection mockUserConnection = mock(UserConnection.class);
    UserDto mockUser = mock(UserDto.class);
    when(mockUser.getId()).thenReturn(FAKE_USER_ID);
    when(mockUserConnection.getCurrentUser()).thenReturn(mockUser);

    // Prepare factoryConnection
    FactoryConnection mockFactoryConnection = mock(FactoryConnection.class);
    FactoryDto gitHubfakeFactory =
        DtoFactory.getInstance()
            .createDtoFromJson(resourceToString("factory-MKTG-341.json"), FactoryDto.class);
    when(mockFactoryConnection.getFactory("fakeFactoryId")).thenReturn(gitHubfakeFactory);
    when(mockFactoryConnection.updateFactory(gitHubfakeFactory)).thenReturn(gitHubfakeFactory);

    // Prepare GitHubWebhookService
    fakeGitHubWebhookService =
        new GitHubWebhookService(
            mockAuthConnection,
            mockFactoryConnection,
            configurationProperties,
            "username",
            "password");
  }

  @Test
  public void testGithubWebhookPushEventNoConnector() throws Exception {
    HttpServletRequest mockRequest = prepareRequest(Service.GITHUB, "push");
    Response response = fakeGitHubWebhookService.handleGithubWebhookEvent(mockRequest);
    Assert.assertTrue(response.getStatus() == NO_CONTENT.getStatusCode());
  }

  @Test
  public void testGithubWebhookPullRequestEventNoConnector() throws Exception {
    HttpServletRequest mockRequest = prepareRequest(Service.GITHUB, "pull_request");
    Response response = fakeGitHubWebhookService.handleGithubWebhookEvent(mockRequest);
    Assert.assertTrue(response.getStatus() == NO_CONTENT.getStatusCode());
  }

  protected HttpServletRequest prepareRequest(Service service, String eventType) throws Exception {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    String eventMessageString = null;
    switch (eventType) {
      case "work_item_created":
        eventMessageString = resourceToString("vsts-work-item-created-event.json");
        break;
      case "pull_request":
        eventMessageString = resourceToString("github-pull-request-event.json");
        break;
      case "push":
        eventMessageString = resourceToString("github-push-event.json");
        break;
      default:
        break;
    }
    ServletInputStream fakeInputStream = null;
    if (eventMessageString != null) {
      ByteArrayInputStream byteArrayInputStream =
          new ByteArrayInputStream(eventMessageString.getBytes(StandardCharsets.UTF_8));
      fakeInputStream = new MockServletInputStream(byteArrayInputStream);
    }
    if (service == Service.GITHUB) {
      when(mockRequest.getHeader(REQUEST_HEADER_GITHUB_EVENT)).thenReturn(eventType);
    }
    when(mockRequest.getInputStream()).thenReturn(fakeInputStream);

    return mockRequest;
  }

  private String resourceToString(String resource) throws Exception {
    final Path resourcePath =
        Paths.get(Thread.currentThread().getContextClassLoader().getResource(resource).toURI());
    return new String(Files.readAllBytes(resourcePath));
  }
}
