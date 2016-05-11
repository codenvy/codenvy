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
import com.codenvy.plugin.pullrequest.client.workflow.SyntheticStep;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.pullrequest.client.vcs.BranchUpToDateException;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.NoPullRequestException;
import com.codenvy.plugin.pullrequest.client.dto.PullRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.ssh.SshServiceClient;

/**
 * Pushes branch to the repository.
 *
 * @author Yevhenii Voevodin
 */
public class PushBranchStep implements SyntheticStep {

    private final Step               delegate;
    private final String             repositoryOwner;
    private final String             repositoryName;
    private final ContributeMessages messages;
    private final DialogFactory      dialogFactory;
    private final SshServiceClient   sshService;

    @AssistedInject
    public PushBranchStep(@Assisted("delegate") Step delegate,
                          @Assisted("repositoryOwner") String repositoryOwner,
                          @Assisted("repositoryName") String repositoryName,
                          ContributeMessages messages,
                          DialogFactory dialogFactory,
                          SshServiceClient sshService) {
        this.delegate = delegate;
        this.repositoryOwner = repositoryOwner;
        this.repositoryName = repositoryName;
        this.messages = messages;
        this.dialogFactory = dialogFactory;
        this.sshService = sshService;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {

        /*
         * Check if a Pull Request with given base and head branches already exists.
         * If there is none, push the contribution branch.
         * If there is one, propose to updatePullRequest the pull request.
         */

        context.getVcsHostingService()
               .getPullRequest(repositoryOwner,
                               repositoryName,
                               context.getHostUserLogin(),
                               context.getWorkBranchName())
               .then(new Operation<PullRequest>() {
                   @Override
                   public void apply(PullRequest pullRequest) throws OperationException {
                       context.setPullRequest(pullRequest);
                       context.getConfiguration().withContributionComment(pullRequest.getDescription());
                       final ConfirmCallback okCallback = new ConfirmCallback() {
                           @Override
                           public void accepted() {
                               pushBranch(executor, context);
                           }
                       };
                       final CancelCallback cancelCallback = new CancelCallback() {
                           @Override
                           public void cancelled() {
                               executor.fail(delegate, context, messages.stepPushBranchCanceling());
                           }
                       };

                       dialogFactory.createConfirmDialog(
                               messages.contributePartConfigureContributionDialogUpdateTitle(),
                               messages.contributePartConfigureContributionDialogUpdateText(
                                       pullRequest.getHeadRef()),
                               okCallback,
                               cancelCallback).show();
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError err) throws OperationException {
                       try {
                           throw err.getCause();
                       } catch (NoPullRequestException ex) {
                           pushBranch(executor, context);
                       } catch (Throwable thr) {
                           executor.fail(delegate, context, thr.getMessage());
                       }
                   }
               });
    }

    private void pushBranch(final WorkflowExecutor workflow, final Context context) {
        context.getVcsService()
               .pushBranch(context.getProject(),
                           context.getForkedRemoteName(),
                           context.getWorkBranchName(),
                           new AsyncCallback<PushResponse>() {
                               @Override
                               public void onSuccess(final PushResponse result) {
                                   workflow.done(delegate, context);
                               }

                               @Override
                               public void onFailure(final Throwable thr) {
                                   try {
                                       throw thr;
                                   } catch (BranchUpToDateException branchUpEx) {
                                       workflow.fail(delegate,
                                                     context,
                                                     messages.stepPushBranchErrorBranchUpToDate());
                                   } catch (Throwable throwable) {
                                       if (throwable.getMessage().contains("Unable get private ssh key")) {
                                           askGenerateSSH(workflow, context);
                                       } else {
                                           workflow.fail(delegate,
                                                         context,
                                                         messages.stepPushBranchErrorPushingBranch(throwable.getLocalizedMessage()));
                                       }
                                   }
                               }
                           });
    }

    private void askGenerateSSH(final WorkflowExecutor executor, final Context context) {
        final ConfirmCallback okCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                generateSSHAndPushBranch(executor, context, context.getVcsHostingService().getHost());
            }
        };

        final CancelCallback cancelCallback = new CancelCallback() {
            @Override
            public void cancelled() {
                executor.fail(delegate, context, messages.stepPushBranchCanceling());
            }
        };

        dialogFactory.createConfirmDialog(messages.contributePartConfigureContributionDialogSshNotFoundTitle(),
                                          messages.contributePartConfigureContributionDialogSshNotFoundText(),
                                          okCallback,
                                          cancelCallback).show();
    }

    private void generateSSHAndPushBranch(final WorkflowExecutor executor, final Context context, String host) {
        sshService.generatePair("git", host)
                  .then(new Operation<SshPairDto>() {
                      @Override
                      public void apply(SshPairDto arg) throws OperationException {
                          pushBranch(executor, context);
                      }
                  })
                  .catchError(new Operation<PromiseError>() {
                      @Override
                      public void apply(PromiseError err) throws OperationException {
                          executor.fail(delegate, context, err.getMessage());
                      }
                  });
    }
}
