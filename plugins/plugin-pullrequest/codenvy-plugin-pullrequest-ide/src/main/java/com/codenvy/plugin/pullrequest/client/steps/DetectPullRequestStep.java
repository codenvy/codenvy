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
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.pullrequest.shared.dto.PullRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;

import static com.codenvy.plugin.pullrequest.client.workflow.WorkflowStatus.READY_TO_UPDATE_PR;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;

/**
 * Detects if pull request exists for current working branch,
 * stops creation workflow if so and toggles update pull request mode.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class DetectPullRequestStep implements Step {

    private final ContributeMessages  messages;
    private final NotificationManager notificationManager;

    @Inject
    public DetectPullRequestStep(ContributeMessages messages,
                                 NotificationManager manager) {
        this.messages = messages;
        this.notificationManager = manager;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        context.getVcsHostingService()
               .getPullRequest(context.getOriginRepositoryOwner(),
                               context.getOriginRepositoryName(),
                               context.getHostUserLogin(),
                               context.getWorkBranchName())
               .then(new Operation<PullRequest>() {
                   @Override
                   public void apply(final PullRequest pr) throws OperationException {
                       notificationManager.notify(messages.stepDetectPrExistsTitle(),
                                                  messages.stepDetectPrExistsTitle(context.getWorkBranchName()),
                                                  StatusNotification.Status.FAIL,
                                                  FLOAT_MODE);
                       context.setPullRequest(pr);
                       context.setPullRequestIssueNumber(pr.getNumber());
                       context.setForkedRepositoryName(context.getOriginRepositoryName());
                       context.setStatus(READY_TO_UPDATE_PR);
                       executor.fail(DetectPullRequestStep.this, context, messages.stepDetectPrExistsTitle());
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(final PromiseError error) throws OperationException {
                       // keep going if pr already exists
                       executor.done(DetectPullRequestStep.this, context);
                   }
               });
    }
}
