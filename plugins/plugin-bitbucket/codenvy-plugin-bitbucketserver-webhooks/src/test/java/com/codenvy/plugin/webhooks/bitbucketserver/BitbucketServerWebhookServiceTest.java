/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.plugin.webhooks.bitbucketserver;

import com.codenvy.plugin.webhooks.AuthConnection;
import com.codenvy.plugin.webhooks.FactoryConnection;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.Changesets;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.Changeset;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.Commit;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.Project;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.PushEvent;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.RefChange;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.Repository;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.User;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.inject.ConfigurationProperties;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

@Listeners(value = MockitoTestNGListener.class)
public class BitbucketServerWebhookServiceTest {

    private BitbucketServerWebhookService service;

    private final Map<String, String> parameters = new HashMap<>();

    @BeforeMethod
    public void setup() throws Exception {
        FactoryConnection factoryConnection = mock(FactoryConnection.class);
        FactoryDto factory = mock(FactoryDto.class);
        WorkspaceConfigDto workspace = mock(WorkspaceConfigDto.class);
        ProjectConfigDto project = mock(ProjectConfigDto.class);
        SourceStorageDto source = mock(SourceStorageDto.class);
        ConfigurationProperties configurationProperties = mock(ConfigurationProperties.class);
        Map<String, String> properties = new HashMap<>();
        properties.put("env.CODENVY_BITBUCKET_SERVER_WEBHOOK_WEBHOOK1_REPOSITORY_URL",
                       "http://owner@bitbucketserver.host/scm/projectkey/repository.git");
        properties.put("env.CODENVY_BITBUCKET_SERVER_WEBHOOK_WEBHOOK1_FACTORY1_ID", "factoryId");
        when(configurationProperties.getProperties(eq("env.CODENVY_BITBUCKET_SERVER_WEBHOOK_.+"))).thenReturn(properties);
        when(factory.getWorkspace()).thenReturn(workspace);
        when(factory.getLink(anyString())).thenReturn(mock(Link.class));
        when(factory.getId()).thenReturn("factoryId");
        when(factoryConnection.getFactory("factoryId")).thenReturn(factory);
        when(factoryConnection.updateFactory(factory)).thenReturn(factory);
        when(workspace.getProjects()).thenReturn(singletonList(project));
        when(project.getSource()).thenReturn(source);
        when(source.getType()).thenReturn("type");
        when(source.getLocation()).thenReturn("http://owner@bitbucketserver.host/scm/projectkey/repository.git");
        parameters.put("branch", "testBranch");
        when(source.getParameters()).thenReturn(parameters);

        service = spy(new BitbucketServerWebhookService(mock(AuthConnection.class),
                                                        factoryConnection,
                                                        configurationProperties,
                                                        "username",
                                                        "password",
                                                        "http://bitbucketserver.host/"));
    }

    @Test
    public void shouldHandlePushEvent() throws Exception {
        //given
        PushEvent pushEvent = createPushEvent("commit");

        //when
        service.handleWebhookEvent(prepareRequest(pushEvent));

        //then
        verify(service).handlePushEvent(anyObject(), anyString());
    }

    @Test
    public void shouldChangeFactoryStartPointFromBranchToCommitWhenMergeCommitDetected() throws Exception {
        //given
        PushEvent pushEvent = createPushEvent("Merge pull request #3 in ~projectkey/repository from testBranch to master");

        //when
        service.handleWebhookEvent(prepareRequest(pushEvent));

        //then
        verify(service).handleMergeEvent(anyObject(), anyString());
        assertFalse(parameters.containsKey("branch"));
        assertEquals(parameters.get("commitId"), "hash commit");
    }

    private HttpServletRequest prepareRequest(PushEvent event) throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(event.toString().getBytes(StandardCharsets.UTF_8));
        ServletInputStream inputStream = new ServletInputStream() {
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
        when(mockRequest.getInputStream()).thenReturn(inputStream);

        return mockRequest;
    }

    private PushEvent createPushEvent(String message) {
        return DtoFactory.newDto(PushEvent.class)
                         .withRepository(DtoFactory.newDto(Repository.class)
                                                   .withProject(DtoFactory.newDto(Project.class)
                                                                          .withOwner(DtoFactory.newDto(User.class)
                                                                                               .withName("owner"))
                                                                          .withKey("projectkey"))
                                                   .withName("repository"))
                         .withRefChanges(singletonList(DtoFactory.newDto(RefChange.class)
                                                                 .withToHash("hash commit")
                                                                 .withType("UPDATE")
                                                                 .withRefId("refs/heads/master")))
                         .withChangesets(DtoFactory.newDto(Changesets.class)
                                                   .withValues(singletonList(DtoFactory.newDto(Changeset.class)
                                                                                       .withToCommit(DtoFactory.newDto(Commit.class)
                                                                                                               .withId("hash commit")
                                                                                                               .withMessage(message)))));
    }
}
