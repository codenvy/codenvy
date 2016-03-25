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
package com.codenvy.plugin.webhooks;

import com.codenvy.plugin.webhooks.github.GitHubWebhookService;

import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.dto.server.DtoFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Unit tests for GitHubWebhookService
 *
 * @author Stephane Tournie
 */
@RunWith(MockitoJUnitRunner.class)
public class TestGitHubWebhookService {

    private final static String REQUEST_HEADER_GITHUB_EVENT = "X-GitHub-Event";
    private final static String FAKE_USER_ID                = "TEST_USER_ID";

    private enum Service {
        GITHUB,
        VSTS
    }

    private GitHubWebhookService fakeGitHubWebhookService;

    @Before
    public void setUp() throws Exception {
        // Prepare authConnection
        Token fakeToken = DtoFactory.newDto(Token.class).withValue("fakeToken");
        AuthConnection mockAuthConnection = mock(AuthConnection.class);
        when(mockAuthConnection.authenticateUser("somebody@somemail.com", "somepwd")).thenReturn(fakeToken);

        // Prepare userConnection
        UserConnection mockUserConnection = mock(UserConnection.class);
        UserDescriptor mockUser = mock(UserDescriptor.class);
        when(mockUser.getId()).thenReturn(FAKE_USER_ID);
        when(mockUserConnection.getCurrentUser()).thenReturn(mockUser);

        // Prepare factoryConnection
        FactoryConnection mockFactoryConnection = mock(FactoryConnection.class);
        Factory gitHubfakeFactory = DtoFactory.getInstance().createDtoFromJson(resourceToString("factory-MKTG-341.json"), Factory.class);
        when(mockFactoryConnection.getFactory("fakeFactoryId")).thenReturn(gitHubfakeFactory);
        when(mockFactoryConnection.updateFactory(gitHubfakeFactory)).thenReturn(gitHubfakeFactory);

        // Prepare GitHubWebhookService
        fakeGitHubWebhookService = new GitHubWebhookService(mockAuthConnection, mockFactoryConnection);
    }

    @Test
    public void testGithubWebhookPushEventNoConnector() throws Exception {
        HttpServletRequest mockRequest = prepareRequest(Service.GITHUB, "push");
        Response response = fakeGitHubWebhookService.handleGithubWebhookEvent(mockRequest);
        Assert.assertTrue(response.getStatus() == OK.getStatusCode());
    }

    @Test
    public void testGithubWebhookPullRequestEventNoConnector() throws Exception {
        HttpServletRequest mockRequest = prepareRequest(Service.GITHUB, "pull_request");
        Response response = fakeGitHubWebhookService.handleGithubWebhookEvent(mockRequest);
        Assert.assertTrue(response.getStatus() == OK.getStatusCode());
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
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(eventMessageString.getBytes(StandardCharsets.UTF_8));
            fakeInputStream = new ServletInputStream() {
                public int read() throws IOException {
                    return byteArrayInputStream.read();
                }
            };
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
