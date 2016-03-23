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
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.pullrequest.client.vcs.VcsService;
import com.codenvy.plugin.pullrequest.client.vcs.VcsServiceProvider;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.NoVcsHostingServiceImplementationException;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingServiceProvider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedEvent;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedHandler;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.plugin.pullrequest.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_TO_BRANCH_VARIABLE_NAME;
import static com.codenvy.plugin.pullrequest.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_ID;
import static java.util.Collections.singletonList;

/**
 * Registers event handlers for adding/removing contribution part.
 *
 * <p>Manages {@code AppContext.getCurrent().}{@link CurrentProject#getRootProject() getRootProject()}
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
    private final AppContext                appContext;
    private final WorkflowExecutor          workflowExecutor;
    private final VcsHostingServiceProvider hostingServiceProvider;
    private final VcsServiceProvider        vcsServiceProvider;
    private final ProjectServiceClient      projectService;
    private final DtoFactory                dtoFactory;

    private String  lastSelectedProjectName;
    private boolean partWasOpened;

    @Inject
    public ContributionExtension(final EventBus eventBus,
                                 final ContributeResources resources,
                                 final AppContext appContext,
                                 final ContributePartPresenter contributionPartPresenter,
                                 final WorkflowExecutor workflow,
                                 final VcsHostingServiceProvider vcsHostingServiceProvider,
                                 final VcsServiceProvider vcsServiceProvider,
                                 final ProjectServiceClient projectService,
                                 final DtoFactory dtoFactory) {
        this.workflowExecutor = workflow;
        this.contributionPartPresenter = contributionPartPresenter;
        this.appContext = appContext;
        this.hostingServiceProvider = vcsHostingServiceProvider;
        this.vcsServiceProvider = vcsServiceProvider;
        this.projectService = projectService;
        this.dtoFactory = dtoFactory;

        eventBus.addHandler(CurrentProjectChangedEvent.TYPE, new CurrentProjectChangedHandler() {
            @Override
            public void onCurrentProjectChanged(CurrentProjectChangedEvent event) {
                final ProjectConfigDto rootProject = appContext.getCurrentProject().getRootProject();
                if (!rootProject.getName().equals(lastSelectedProjectName) || !partWasOpened) {
                    initializeContributorExtension(copy(rootProject)); //here need to use copy of object because it can be changed in other thread
                }
                lastSelectedProjectName = rootProject.getName();
            }
        });

        resources.contributeCss().ensureInjected();
    }

    private ProjectConfigDto copy(ProjectConfigDto origin) {
        String json = dtoFactory.toJson(origin);
        ProjectConfigDto copy = dtoFactory.createDtoFromJson(json, ProjectConfigDto.class);
        return copy;
    }

    private void initializeContributorExtension(final ProjectConfigDto project) {
        hostingServiceProvider.getVcsHostingService(project)
                              .then(initContribution(project))
                              .catchError(cancelInit(project));
    }

    private Operation<PromiseError> cancelInit(final ProjectConfigDto project) {
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

    private Promise<ProjectConfigDto> addMixin(final ProjectConfigDto project) {
        if (!project.getMixins().contains(CONTRIBUTION_PROJECT_TYPE_ID)) {
            final VcsService vcsService = vcsServiceProvider.getVcsService(project);
            return vcsService.getBranchName(project)
                             .thenPromise(new Function<String, Promise<ProjectConfigDto>>() {
                                 @Override
                                 public Promise<ProjectConfigDto> apply(String branchName) throws FunctionException {
                                     project.getMixins().add(CONTRIBUTION_PROJECT_TYPE_ID);
                                     project.getAttributes().put(CONTRIBUTE_TO_BRANCH_VARIABLE_NAME, singletonList(branchName));
                                     return projectService.updateProject(appContext.getWorkspaceId(),
                                                                         project.getPath(),
                                                                         project);
                                 }
                             });
        } else {
            return Promises.resolve(project);
        }
    }

    private Operation<VcsHostingService> initContribution(final ProjectConfigDto project) {
        return new Operation<VcsHostingService>() {
            @Override
            public void apply(final VcsHostingService vcsHostingService) throws OperationException {
                addMixin(project)
                        .then(new Operation<ProjectConfigDto>() {
                            @Override
                            public void apply(ProjectConfigDto project) throws OperationException {
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
