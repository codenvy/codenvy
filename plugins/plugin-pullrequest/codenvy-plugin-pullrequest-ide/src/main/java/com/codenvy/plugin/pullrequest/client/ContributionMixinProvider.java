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
import com.codenvy.plugin.pullrequest.client.vcs.VcsService;
import com.codenvy.plugin.pullrequest.client.vcs.VcsServiceProvider;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.SelectionChangedEvent;
import org.eclipse.che.ide.api.event.SelectionChangedHandler;
import org.eclipse.che.ide.api.factory.FactoryAcceptedEvent;
import org.eclipse.che.ide.api.factory.FactoryAcceptedHandler;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;

import static com.codenvy.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTE_TO_BRANCH_VARIABLE_NAME;
import static com.codenvy.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_ID;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.constraints.Constraints.FIRST;
import static org.eclipse.che.ide.api.parts.PartStackType.TOOLING;

/**
 * Responsible for setting up contribution mixin for the currently selected project in application context.
 *
 * @author Vlad Zhukovskyi
 * @since 5.0.0
 */
@Singleton
public class ContributionMixinProvider {

    private final EventBus                  eventBus;
    private final AppContext                appContext;
    private final WorkspaceAgent            workspaceAgent;
    private final ContributePartPresenter   contributePart;
    private final WorkflowExecutor          workflowExecutor;
    private final VcsServiceProvider        vcsServiceProvider;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;

    private HandlerRegistration handlerRegistration;

    private Project lastSelected;

    @Inject
    public ContributionMixinProvider(EventBus eventBus,
                                     AppContext appContext,
                                     WorkspaceAgent workspaceAgent,
                                     ContributePartPresenter contributePart,
                                     WorkflowExecutor workflowExecutor,
                                     VcsServiceProvider vcsServiceProvider,
                                     VcsHostingServiceProvider vcsHostingServiceProvider) {
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.workspaceAgent = workspaceAgent;
        this.contributePart = contributePart;
        this.workflowExecutor = workflowExecutor;
        this.vcsServiceProvider = vcsServiceProvider;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;

        if (appContext.getFactory() != null) {
            handlerRegistration = eventBus.addHandler(FactoryAcceptedEvent.TYPE, new FactoryAcceptedHandler() {
                @Override
                public void onFactoryAccepted(FactoryAcceptedEvent event) {
                    handlerRegistration.removeHandler();

                    subscribeToSelectionChangedEvent();
                }
            });
        } else {
            handlerRegistration = eventBus.addHandler(WorkspaceReadyEvent.getType(), new WorkspaceReadyEvent.WorkspaceReadyHandler() {
                @Override
                public void onWorkspaceReady(WorkspaceReadyEvent event) {
                    handlerRegistration.removeHandler();

                    subscribeToSelectionChangedEvent();
                }
            });
        }
    }

    private void subscribeToSelectionChangedEvent() {
        eventBus.addHandler(SelectionChangedEvent.TYPE, new SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                processCurrentProject();
            }
        });
    }

    void processCurrentProject() {
        final Project rootProject = appContext.getRootProject();

        if (lastSelected != null && lastSelected.equals(rootProject)) {
            return;
        }

        final PartStack toolingPartStack = workspaceAgent.getPartStack(TOOLING);

        if (rootProject == null) {

            if (toolingPartStack.containsPart(contributePart)) {
                invalidateContext(lastSelected);
                hidePart();
            }
        } else if (hasVcsService(rootProject)) {

            if (hasContributionMixin(rootProject)) {

                vcsHostingServiceProvider.getVcsHostingService(rootProject).then(new Operation<VcsHostingService>() {
                    @Override
                    public void apply(VcsHostingService vcsHostingService) throws OperationException {
                        workflowExecutor.init(vcsHostingService, rootProject);
                        addPart(toolingPartStack);
                    }
                });
            } else {
                vcsHostingServiceProvider.getVcsHostingService(rootProject)
                                         .then(new Operation<VcsHostingService>() {
                                             @Override
                                             public void apply(final VcsHostingService vcsHostingService)
                                                     throws OperationException {
                                                 addMixin(rootProject)
                                                         .then(new Operation<Project>() {
                                                             @Override
                                                             public void apply(Project project) throws OperationException {
                                                                 workflowExecutor.init(vcsHostingService, project);
                                                                 addPart(toolingPartStack);

                                                                 lastSelected = project;
                                                             }
                                                         })
                                                         .catchError(new Operation<PromiseError>() {
                                                             @Override
                                                             public void apply(final PromiseError error) throws OperationException {
                                                                 invalidateContext(rootProject);
                                                                 hidePart();
                                                             }
                                                         });
                                             }
                                         })
                                         .catchError(new Operation<PromiseError>() {
                                             @Override
                                             public void apply(final PromiseError error) throws OperationException {
                                                 invalidateContext(rootProject);
                                                 hidePart();
                                             }
                                         });
            }

        } else {
            invalidateContext(rootProject);
            hidePart();
        }

        lastSelected = rootProject;
    }

    private void invalidateContext(Project project) {
        final Optional<Context> context = workflowExecutor.getContext(project.getName());
        if (context.isPresent()) {
            workflowExecutor.invalidateContext(context.get().getProject());
        }
    }

    private void hidePart() {
        workspaceAgent.hidePart(contributePart);
        workspaceAgent.removePart(contributePart);
    }

    private void addPart(PartStack partStack) {
        if (!partStack.containsPart(contributePart)) {
            partStack.addPart(contributePart, FIRST);
        }
    }

    private boolean hasVcsService(Project project) {
        return vcsServiceProvider.getVcsService(project) != null;
    }

    private boolean hasContributionMixin(Project project) {
        return project.getMixins().contains(CONTRIBUTION_PROJECT_TYPE_ID);
    }

    private Promise<Project> addMixin(final Project project) {
        final VcsService vcsService = vcsServiceProvider.getVcsService(project);

        if (vcsService == null || project.getMixins().contains(CONTRIBUTION_PROJECT_TYPE_ID)) {
            return Promises.resolve(project);
        }

        return vcsService.getBranchName(project)
                         .thenPromise(new Function<String, Promise<Project>>() {
                             @Override
                             public Promise<Project> apply(String branchName) throws FunctionException {
                                 MutableProjectConfig mutableConfig = new MutableProjectConfig(project);
                                 mutableConfig.getMixins().add(CONTRIBUTION_PROJECT_TYPE_ID);
                                 mutableConfig.getAttributes().put(CONTRIBUTE_TO_BRANCH_VARIABLE_NAME,
                                                                   singletonList(branchName));

                                 return project.update().withBody(mutableConfig).send();
                             }
                         });
    }
}
