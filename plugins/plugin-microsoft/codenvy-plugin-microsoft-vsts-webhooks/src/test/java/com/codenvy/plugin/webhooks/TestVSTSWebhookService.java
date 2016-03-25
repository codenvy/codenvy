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

import com.codenvy.plugin.webhooks.vsts.VSTSConnection;
import com.codenvy.plugin.webhooks.vsts.VSTSWebhookService;
import com.google.common.collect.ImmutableList;

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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Unit tests for TestVSTSWebhookService
 *
 * @author Stephane Tournie
 */
@RunWith(MockitoJUnitRunner.class)
public class TestVSTSWebhookService {

    private final static String FAKE_USER_ID = "TEST_USER_ID";

    private VSTSWebhookService fakeVSTSWebhookService;

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
        Factory VSTSfakeFactory = DtoFactory.getInstance().createDtoFromJson(resourceToString("factory-codenvy.json"), Factory.class);
        when(mockFactoryConnection.findFactory("codenvy", FAKE_USER_ID)).thenReturn(ImmutableList.of(VSTSfakeFactory));
        when(mockFactoryConnection.saveFactory(anyObject())).thenReturn(VSTSfakeFactory);
        when(mockFactoryConnection.getFactory(VSTSfakeFactory.getId())).thenReturn(VSTSfakeFactory);
        when(mockFactoryConnection.updateFactory(VSTSfakeFactory)).thenReturn(VSTSfakeFactory);

        // Prepare VSTSConnection
        VSTSConnection mockVSTSConnection = mock(VSTSConnection.class);
        when(mockVSTSConnection.getRepositoryNameUrl("https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/_apis/git/repositories/278d5cd2-584d-4b63-824a-2ba458937249", "2.2-preview.1",
                                                     org.eclipse.che.commons.lang.Pair.of("username", "password"))).thenReturn("https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/test-project");

        // Prepare VSTSWebhookService
        fakeVSTSWebhookService =
                new VSTSWebhookService(mockAuthConnection, mockFactoryConnection, mockUserConnection, mockVSTSConnection);
    }

    @Test
    public void testVSTSWebhookWorkItemCreatedEventNoConnector() throws Exception {
        HttpServletRequest mockRequest = prepareRequest("work_item_created");
        Response response = fakeVSTSWebhookService.handleVSTSWebhookEvent(mockRequest);
        Assert.assertTrue(response.getStatus() == OK.getStatusCode());
    }

    @Test
    public void testVSTSWebhookPullRequestUpdatedNoConnector() throws Exception {
        HttpServletRequest mockRequest = prepareRequest("pull_request_updated");
        Response response = fakeVSTSWebhookService.handleVSTSWebhookEvent(mockRequest);
        Assert.assertTrue(response.getStatus() == OK.getStatusCode());
    }

    protected HttpServletRequest prepareRequest(String eventType) throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        String eventMessageString = null;
        switch (eventType) {
            case "work_item_created":
                eventMessageString = resourceToString("vsts-work-item-created-event.json");
                break;
            case "pull_request_updated":
                eventMessageString = resourceToString("vsts-pullrequest-updated-event.json");
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
        when(mockRequest.getInputStream()).thenReturn(fakeInputStream);

        return mockRequest;
    }

    private String resourceToString(String resource) throws Exception {
        final Path resourcePath =
                Paths.get(Thread.currentThread().getContextClassLoader().getResource(resource).toURI());
        return new String(Files.readAllBytes(resourcePath));
    }
}
