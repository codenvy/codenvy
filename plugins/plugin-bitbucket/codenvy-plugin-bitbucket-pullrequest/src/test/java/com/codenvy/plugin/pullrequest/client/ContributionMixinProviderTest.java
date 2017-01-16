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
package com.codenvy.plugin.pullrequest.client;

import com.codenvy.plugin.pullrequest.client.parts.contribute.ContributePartPresenter;
import com.codenvy.plugin.pullrequest.client.vcs.VcsService;
import com.codenvy.plugin.pullrequest.client.vcs.VcsServiceProvider;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.Project;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link ContributionMixinProvider}
 */
@Listeners(MockitoTestNGListener.class)
public class ContributionMixinProviderTest {

    @Mock
    EventBus                  eventBus;
    @Mock
    AppContext                appContext;
    @Mock
    WorkspaceAgent            workspaceAgent;
    @Mock
    ContributePartPresenter   contributePartPresenter;
    @Mock
    WorkflowExecutor          workflowExecutor;
    @Mock
    VcsServiceProvider        vcsServiceProvider;
    @Mock
    VcsHostingServiceProvider vcsHostingServiceProvider;

    @Mock
    Project                project;
    @Mock
    PartStack              toolingPartStack;
    @Mock
    VcsService             vcsService;
    @Mock
    VcsHostingService      vcsHostingService;
    @Mock
    Project.ProjectRequest projectRequest;

    @Mock
    Promise<VcsHostingService> vcsHostingServicePromise;
    @Mock
    Promise<String>            branchPromise;
    @Mock
    Promise<Project>           projectPromise;

    @Captor
    ArgumentCaptor<Operation<VcsHostingService>>       vcsOperationArgumentCaptor;
    @Captor
    ArgumentCaptor<Function<String, Promise<Project>>> branchCaptor;
    @Captor
    ArgumentCaptor<Operation<Project>>                 projectCaptor;

    private ContributionMixinProvider provider;

    @BeforeMethod
    public void setUp() throws Exception {
        provider = new ContributionMixinProvider(eventBus,
                                                 appContext,
                                                 workspaceAgent,
                                                 contributePartPresenter,
                                                 workflowExecutor,
                                                 vcsServiceProvider,
                                                 vcsHostingServiceProvider);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testShouldSetupMixinForTheProjectAndAddPart() throws Exception {
        when(appContext.getRootProject()).thenReturn(project);
        when(vcsServiceProvider.getVcsService(eq(project))).thenReturn(vcsService);
        when(project.getMixins()).thenReturn(emptyList());
        when(vcsHostingServiceProvider.getVcsHostingService(eq(project))).thenReturn(vcsHostingServicePromise);
        when(vcsHostingServicePromise.then(any(Operation.class))).thenReturn(vcsHostingServicePromise);
        when(vcsService.getBranchName(eq(project))).thenReturn(branchPromise);
        when(branchPromise.<Project>thenPromise(any(Function.class))).thenReturn(projectPromise);
        when(projectPromise.<PromiseError>catchError(any(Operation.class))).thenReturn(projectPromise);
        when(projectPromise.then(any(Operation.class))).thenReturn(projectPromise);
        when(workspaceAgent.getPartStack(eq(PartStackType.TOOLING))).thenReturn(toolingPartStack);
        when(toolingPartStack.containsPart(eq(contributePartPresenter))).thenReturn(false);

        provider.processCurrentProject();

        verify(vcsHostingServicePromise).then(vcsOperationArgumentCaptor.capture());
        vcsOperationArgumentCaptor.getValue().apply(vcsHostingService);

        verify(projectPromise).then(projectCaptor.capture());
        projectCaptor.getValue().apply(project);

        verify(workflowExecutor).init(eq(vcsHostingService), eq(project));
        verify(toolingPartStack).addPart(eq(contributePartPresenter), eq(Constraints.FIRST));
    }

}