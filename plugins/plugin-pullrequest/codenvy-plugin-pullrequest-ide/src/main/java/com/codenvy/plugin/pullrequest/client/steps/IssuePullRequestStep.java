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
import com.codenvy.plugin.pullrequest.client.vcs.hosting.NoCommitsInPullRequestException;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.NoHistoryInCommonException;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.PullRequestAlreadyExistsException;
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.pullrequest.shared.dto.Configuration;
import com.codenvy.plugin.pullrequest.shared.dto.PullRequest;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;

import javax.inject.Inject;

/**
 * Create the pull request on the remote VCS repository.
 *
 * @author Kevin Pollet
 */
@Singleton
public class IssuePullRequestStep implements Step {
    private final ContributeMessages messages;

    @Inject
    public IssuePullRequestStep(final ContributeMessages messages) {
        this.messages = messages;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        final Configuration configuration = context.getConfiguration();
        context.getVcsHostingService()
               .createPullRequest(context.getUpstreamRepositoryOwner(),
                                  context.getUpstreamRepositoryName(),
                                  context.getHostUserLogin(),
                                  context.getWorkBranchName(),
                                  context.getContributeToBranchName(),
                                  configuration.getContributionTitle(),
                                  configuration.getContributionComment())
               .then(new Operation<PullRequest>() {
                   @Override
                   public void apply(PullRequest pullRequest) throws OperationException {
                       context.setPullRequestIssueNumber(pullRequest.getNumber());
                       context.setPullRequest(pullRequest);
                       executor.done(IssuePullRequestStep.this, context);
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(final PromiseError exception) throws OperationException {
                       try {
                           throw exception.getCause();
                       } catch (PullRequestAlreadyExistsException | NoHistoryInCommonException ex) {
                           executor.fail(IssuePullRequestStep.this,
                                         context,
                                         ex.getLocalizedMessage());
                       } catch (NoCommitsInPullRequestException noCommitsEx) {
                           executor.fail(IssuePullRequestStep.this,
                                         context,
                                         messages.stepIssuePullRequestErrorCreatePullRequestWithoutCommits());
                       } catch (Throwable thr) {
                           executor.fail(IssuePullRequestStep.this,
                                         context,
                                         messages.stepIssuePullRequestErrorCreatePullRequest());
                       }
                   }
               });
    }
}
