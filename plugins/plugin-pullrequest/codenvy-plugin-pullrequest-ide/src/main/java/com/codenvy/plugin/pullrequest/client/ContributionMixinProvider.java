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
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceInterceptor;

import static com.codenvy.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTE_TO_BRANCH_VARIABLE_NAME;
import static com.codenvy.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_ID;
import static java.util.Collections.singletonList;

/**
 * Interceptor component, which pre-process resource at loading step.
 * <p>
 * Loading step means, that resource (file, folder or project) which is loaded from server should go through registered interceptors
 * to register on the client side.
 * <p>
 * Current interceptor modifies resource (in our case resource - project) and set up contribution mixin.
 *
 * @author Vlad Zhukovskyi
 * @see ResourceInterceptor
 * @since 5.0.0
 */
@Singleton
public class ContributionMixinProvider implements ResourceInterceptor {

    private VcsServiceProvider        vcsServiceProvider;
    private VcsHostingServiceProvider vcsHostingServiceProvider;
    private WorkflowExecutor          workflowExecutor;
    private ContributePartPresenter   contributionPartPresenter;

    @Inject
    public ContributionMixinProvider(VcsServiceProvider vcsServiceProvider,
                                     VcsHostingServiceProvider vcsHostingServiceProvider,
                                     WorkflowExecutor workflowExecutor,
                                     ContributePartPresenter contributionPartPresenter) {
        this.vcsServiceProvider = vcsServiceProvider;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.workflowExecutor = workflowExecutor;
        this.contributionPartPresenter = contributionPartPresenter;
    }

    /** {@inheritDoc} */
    @Override
    public void intercept(Resource resource) {
        if (!resource.isProject()) {
            return;
        }

        final Project project = (Project)resource;

        if (isNotRootProject(project) || hasContributionMixin(project) || hasNoVcsService(project)) {
            return;
        }

        vcsHostingServiceProvider.getVcsHostingService(project)
                                 .then(new Operation<VcsHostingService>() {
                                     @Override
                                     public void apply(final VcsHostingService vcsHostingService) throws OperationException {
                                         addMixin(project)
                                                 .then(new Operation<Project>() {
                                                     @Override
                                                     public void apply(Project project) throws OperationException {
                                                         contributionPartPresenter.open();
                                                         workflowExecutor.init(vcsHostingService, project);
                                                     }
                                                 })
                                                 .catchError(new Operation<PromiseError>() {
                                                     @Override
                                                     public void apply(final PromiseError error) throws OperationException {
                                                         workflowExecutor.invalidateContext(project);
                                                     }
                                                 });
                                     }
                                 })
                                 .catchError(new Operation<PromiseError>() {
                                     @Override
                                     public void apply(final PromiseError error) throws OperationException {
                                         workflowExecutor.invalidateContext(project);
                                     }
                                 });
    }

    private boolean isNotRootProject(Project project) {
        return project.getLocation().segmentCount() != 1;
    }

    private boolean hasContributionMixin(Project project) {
        return project.getMixins().contains(CONTRIBUTION_PROJECT_TYPE_ID);
    }

    private boolean hasNoVcsService(Project project) {
        return vcsServiceProvider.getVcsService(project) == null;
    }

    private Promise<Project> addMixin(final Project project) {
        final VcsService vcsService = vcsServiceProvider.getVcsService(project);

        if (vcsService == null || project.getMixins().contains(CONTRIBUTION_PROJECT_TYPE_ID)) {
            return Promises.resolve(project);
        }

        final Promise<Project> projectPromise = vcsService.getBranchName(project)
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

        return projectPromise;
    }
}
