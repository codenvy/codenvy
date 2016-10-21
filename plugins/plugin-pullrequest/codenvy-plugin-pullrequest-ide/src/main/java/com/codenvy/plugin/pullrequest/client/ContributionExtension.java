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
package com.codenvy.plugin.pullrequest.client;

import com.codenvy.plugin.pullrequest.client.parts.contribute.ContributePartPresenter;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.SelectionChangedEvent;
import org.eclipse.che.ide.api.event.SelectionChangedHandler;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_ID;

/**
 * Registers event handlers for adding/removing contribution part.
 *
 * <p>Manages {@code AppContext#getRootProject}
 * current root project state, in the case of adding and removing 'contribution' mixin.
 * Contribution mixin itself is 'synthetic' one and needed only for managing plugin specific project attributes.
 *
 * @author Stephane Tournie
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ContributionExtension {

    private final WorkflowExecutor          workflowExecutor;

    private Project lastSelectedProject;

    @Inject
    public ContributionExtension(final EventBus eventBus,
                                 final ContributeResources resources,
                                 final AppContext appContext,
                                 final WorkflowExecutor workflow,
                                 final VcsHostingServiceProvider vcsHostingServiceProvider,
                                 final ContributePartPresenter contributePart,
                                 final WorkspaceAgent workspaceAgent) {
        this.workflowExecutor = workflow;

        eventBus.addHandler(SelectionChangedEvent.TYPE, new SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                final Project rootProject = appContext.getRootProject();
                if (rootProject == null) {
                    workspaceAgent.getPartStack(PartStackType.TOOLING).removePart(contributePart);
                    return;
                }

                if (rootProject.getMixins().contains(CONTRIBUTION_PROJECT_TYPE_ID) && !rootProject.equals(lastSelectedProject)) {
                    vcsHostingServiceProvider.getVcsHostingService(rootProject).then(new Operation<VcsHostingService>() {
                        @Override
                        public void apply(VcsHostingService arg) throws OperationException {
                            final PartStack partStack = workspaceAgent.getPartStack(PartStackType.TOOLING);
                            if (partStack.getActivePart() == null || !partStack.getActivePart().equals(contributePart)) {
                                partStack.addPart(contributePart);
                            }
                            workflowExecutor.init(arg, rootProject);
                        }
                    });

                }

                lastSelectedProject = rootProject;
            }
        });

        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, new WorkspaceStoppedEvent.Handler() {
            @Override
            public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
                if (lastSelectedProject != null) {
                    workflowExecutor.invalidateContext(lastSelectedProject);
                    contributePart.minimize();
                }
            }
        });

        resources.contributeCss().ensureInjected();
    }
}
