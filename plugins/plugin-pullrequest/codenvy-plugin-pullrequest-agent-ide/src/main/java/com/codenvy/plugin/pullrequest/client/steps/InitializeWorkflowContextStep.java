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
package com.codenvy.plugin.pullrequest.client.steps;

import com.codenvy.plugin.pullrequest.client.ContributeMessages;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.pullrequest.client.vcs.VcsServiceProvider;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.notification.NotificationManager;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.codenvy.plugin.pullrequest.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_TO_BRANCH_VARIABLE_NAME;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * This step initialize the contribution workflow context.
 *
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
public class InitializeWorkflowContextStep implements Step {

    private static final Predicate<Remote> ORIGIN_REMOTE_FILTER = new Predicate<Remote>() {
        @Override
        public boolean apply(Remote remote) {
            return remote.getName().equals("origin");
        }
    };

    private final VcsServiceProvider  vcsServiceProvider;
    private final NotificationManager notificationManager;
    private final ContributeMessages  messages;

    @Inject
    public InitializeWorkflowContextStep(final VcsServiceProvider vcsServiceProvider,
                                         final NotificationManager notificationManager,
                                         final ContributeMessages messages) {
        this.vcsServiceProvider = vcsServiceProvider;
        this.notificationManager = notificationManager;
        this.messages = messages;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        vcsServiceProvider.getVcsService(context.getProject())
                          .listRemotes(context.getProject())
                          .then(setUpOriginRepoOp(executor, context))
                          .catchError(errorSettingUpOriginRepoOp(executor, context));
    }

    private Operation<List<Remote>> setUpOriginRepoOp(final WorkflowExecutor executor, final Context context) {
        return new Operation<List<Remote>>() {
            @Override
            public void apply(final List<Remote> remotes) throws OperationException {
                final Optional<Remote> remoteOpt = FluentIterable.from(remotes)
                                                                 .filter(ORIGIN_REMOTE_FILTER)
                                                                 .first();
                if (remoteOpt.isPresent()) {
                    final Remote remote = remoteOpt.get();
                    final String originUrl = remote.getUrl();
                    final VcsHostingService vcsHostingService = context.getVcsHostingService();

                    context.setOriginRepositoryOwner(vcsHostingService.getRepositoryOwnerFromUrl(originUrl));
                    context.setOriginRepositoryName(vcsHostingService.getRepositoryNameFromUrl(originUrl));

                    context.setContributeToBranchName(getContributeToBranchName(context.getProject()));

                    executor.done(InitializeWorkflowContextStep.this, context);
                } else {
                    notificationManager.notify(messages.stepInitWorkflowOriginRemoteNotFound(), FAIL, true);
                    executor.fail(InitializeWorkflowContextStep.this, context, messages.stepInitWorkflowOriginRemoteNotFound());
                }
            }
        };
    }

    private String getContributeToBranchName(final ProjectConfigDto project) {
        final Map<String, List<String>> attrs = project.getAttributes();
        if (attrs.containsKey(CONTRIBUTE_TO_BRANCH_VARIABLE_NAME) && !attrs.get(CONTRIBUTE_TO_BRANCH_VARIABLE_NAME).isEmpty()) {
            return attrs.get(CONTRIBUTE_TO_BRANCH_VARIABLE_NAME).get(0);
        }
        if (project.getSource() != null) {
            final String branchName = project.getSource().getParameters().get("branch");
            if (!isNullOrEmpty(branchName)) {
                return branchName;
            }
        }
        return null;
    }

    private Operation<PromiseError> errorSettingUpOriginRepoOp(final WorkflowExecutor executor, final Context context) {
        return new Operation<PromiseError>() {
            @Override
            public void apply(final PromiseError error) throws OperationException {
                notificationManager.notify(messages.contributorExtensionErrorSetupOriginRepository(error.getMessage()),
                                           FAIL,
                                           true);
                executor.fail(InitializeWorkflowContextStep.this, context, error.getMessage());
            }
        };
    }
}
