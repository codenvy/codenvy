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
import com.codenvy.plugin.pullrequest.client.dialogs.commit.CommitPresenter;
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.pullrequest.shared.dto.Configuration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.notification.NotificationManager;

import javax.inject.Inject;

import static com.codenvy.plugin.pullrequest.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.CANCEL;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * This step allow the user to commit the current working tree if the git repository status is not clean.
 *
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
public class CommitWorkingTreeStep implements Step {
    private final CommitPresenter               commitPresenter;
    private final ContributeMessages            messages;
    private final NotificationManager           notificationManager;

    @Inject
    public CommitWorkingTreeStep(final CommitPresenter commitPresenter,
                                 final ContributeMessages messages,
                                 final NotificationManager notificationManager) {
        this.commitPresenter = commitPresenter;
        this.messages = messages;
        this.notificationManager = notificationManager;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        final Configuration configuration = context.getConfiguration();

        commitPresenter.setCommitActionHandler(new CommitPresenter.CommitActionHandler() {
            @Override
            public void onCommitAction(final CommitAction action) {
                if (action == CANCEL) {
                    executor.fail(CommitWorkingTreeStep.this, context, messages.stepCommitCanceled());
                } else {
                    executor.done(CommitWorkingTreeStep.this, context);
                }
            }
        });
        commitPresenter.hasUncommittedChanges(new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(final Throwable exception) {
                notificationManager.notify(exception.getLocalizedMessage(), FAIL, FLOAT_MODE);
                executor.fail(CommitWorkingTreeStep.this, context, exception.getLocalizedMessage());
            }

            @Override
            public void onSuccess(final Boolean hasUncommittedChanges) {
                if (hasUncommittedChanges) {
                    commitPresenter
                            .showView(messages.contributorExtensionDefaultCommitDescription(configuration.getContributionBranchName(),
                                                                                            configuration.getContributionTitle()));
                } else {
                    executor.done(CommitWorkingTreeStep.this, context);
                }
            }
        });
    }
}
