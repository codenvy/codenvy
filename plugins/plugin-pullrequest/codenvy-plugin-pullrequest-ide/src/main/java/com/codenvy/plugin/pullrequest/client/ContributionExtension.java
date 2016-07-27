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
import com.codenvy.plugin.pullrequest.client.vcs.hosting.NoVcsHostingServiceImplementationException;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.web.bindery.event.shared.EventBus;

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
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.plugin.pullrequest.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_TO_BRANCH_VARIABLE_NAME;
import static com.codenvy.plugin.pullrequest.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_ID;
import static java.util.Collections.singletonList;

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

    private final ContributePartPresenter   contributionPartPresenter;
    private final WorkflowExecutor          workflowExecutor;
    private final VcsHostingServiceProvider hostingServiceProvider;
    private final VcsServiceProvider        vcsServiceProvider;

    private String  lastSelectedProjectName;
    private boolean partWasOpened;

    @Inject
    public ContributionExtension(final EventBus eventBus,
                                 final ContributeResources resources,
                                 final AppContext appContext,
                                 final ContributePartPresenter contributionPartPresenter,
                                 final WorkflowExecutor workflow,
                                 final VcsHostingServiceProvider vcsHostingServiceProvider,
                                 final VcsServiceProvider vcsServiceProvider) {
        this.workflowExecutor = workflow;
        this.contributionPartPresenter = contributionPartPresenter;
        this.hostingServiceProvider = vcsHostingServiceProvider;
        this.vcsServiceProvider = vcsServiceProvider;

        eventBus.addHandler(SelectionChangedEvent.TYPE, new SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                final Project rootProject = appContext.getRootProject();
                if (rootProject == null) {
                    return;
                }
                if (!rootProject.getName().equals(lastSelectedProjectName) || !partWasOpened) {
                    initializeContributorExtension(rootProject);
                }
                lastSelectedProjectName = rootProject.getName();
            }
        });

        resources.contributeCss().ensureInjected();
    }

    private void initializeContributorExtension(final Project project) {
        hostingServiceProvider.getVcsHostingService(project)
                              .then(initContribution(project))
                              .catchError(cancelInit(project));
    }

    private Operation<PromiseError> cancelInit(final Project project) {
        return new Operation<PromiseError>() {
            @Override
            public void apply(final PromiseError error) throws OperationException {
                Log.info(getClass(), "INIT ERROR :: " + project.getName() + " :: message => " + error.getMessage());
                try {
                    throw error.getCause();
                } catch (NoVcsHostingServiceImplementationException noVcsEx) {
                    contributionPartPresenter.remove();
                    workflowExecutor.invalidateContext(project);
                } catch (Throwable throwable) {
                    lastSelectedProjectName = null;
                    contributionPartPresenter.remove();
                    workflowExecutor.invalidateContext(project);
                }
            }
        };
    }

    private Promise<Project> addMixin(final Project project) {
        if (!project.getMixins().contains(CONTRIBUTION_PROJECT_TYPE_ID)) {
            final VcsService vcsService = vcsServiceProvider.getVcsService(project);
            return vcsService.getBranchName(project)
                             .thenPromise(new Function<String, Promise<Project>>() {
                                 @Override
                                 public Promise<Project> apply(String branchName) throws FunctionException {
                                     MutableProjectConfig mutableConfig = new MutableProjectConfig(project);
                                     mutableConfig.getMixins().add(CONTRIBUTION_PROJECT_TYPE_ID);
                                     mutableConfig.getAttributes().put(CONTRIBUTE_TO_BRANCH_VARIABLE_NAME, singletonList(branchName));

                                     return project.update().withBody(mutableConfig).send();
                                 }
                             });
        } else {
            return Promises.resolve(project);
        }
    }

    private Operation<VcsHostingService> initContribution(final Project project) {
        return new Operation<VcsHostingService>() {
            @Override
            public void apply(final VcsHostingService vcsHostingService) throws OperationException {
                addMixin(project)
                        .then(new Operation<Project>() {
                            @Override
                            public void apply(Project project) throws OperationException {
                                Log.info(getClass(), "INIT SUCCESSFUL :: " + project.getName());
                                contributionPartPresenter.open();
                                partWasOpened = true;
                                workflowExecutor.init(vcsHostingService, project);
                            }
                        })
                        .catchError(cancelInit(project));
            }
        };
    }
}
