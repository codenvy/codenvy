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

import com.codenvy.plugin.pullrequest.client.vcs.VcsService;
import com.codenvy.plugin.pullrequest.client.vcs.VcsServiceProvider;
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.notification.NotificationManager;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * This step defines the working branch for the user contribution.
 *
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
public class DefineWorkBranchStep implements Step {

    private final NotificationManager notificationManager;
    private final VcsServiceProvider  vcsServiceProvider;

    @Inject
    public DefineWorkBranchStep(final NotificationManager notificationManager, final VcsServiceProvider vcsServiceProvider) {
        this.notificationManager = notificationManager;
        this.vcsServiceProvider = vcsServiceProvider;
    }

    @Override
    public void execute(@NotNull final WorkflowExecutor executor, final Context context) {
        final VcsService vcsService = vcsServiceProvider.getVcsService(context.getProject());

        vcsService.getBranchName(context.getProject())
                  .then(new Operation<String>() {
                      @Override
                      public void apply(String branchName) throws OperationException {
                          if (context.getContributeToBranchName() == null) {
                              context.setContributeToBranchName(branchName);
                          }
                          context.setWorkBranchName(branchName);
                          executor.done(DefineWorkBranchStep.this, context);
                      }
                  })
                  .catchError(new Operation<PromiseError>() {
                      @Override
                      public void apply(PromiseError err) throws OperationException {
                          notificationManager.notify(err.getCause().getLocalizedMessage(), FAIL, FLOAT_MODE);
                          executor.fail(DefineWorkBranchStep.this, context, err.getCause().getLocalizedMessage());
                      }
                  });
    }
}
