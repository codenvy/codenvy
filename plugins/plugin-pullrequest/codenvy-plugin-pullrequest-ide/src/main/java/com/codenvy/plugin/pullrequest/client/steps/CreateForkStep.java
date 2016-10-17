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
import com.codenvy.plugin.pullrequest.client.vcs.hosting.NoUserForkException;
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.pullrequest.shared.dto.Repository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;

/**
 * Create a fork of the contributed project (upstream) to push the user's contribution.
 */
@Singleton
public class CreateForkStep implements Step {
    private final ContributeMessages messages;

    @Inject
    public CreateForkStep(final ContributeMessages messages) {
        this.messages = messages;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        final String originRepositoryOwner = context.getOriginRepositoryOwner();
        final String originRepositoryName = context.getOriginRepositoryName();
        final String upstreamRepositoryOwner = context.getUpstreamRepositoryOwner();
        final String upstreamRepositoryName = context.getUpstreamRepositoryName();

        // the upstream repository has been cloned a fork must be created
        if (originRepositoryOwner.equalsIgnoreCase(upstreamRepositoryOwner) &&
            originRepositoryName.equalsIgnoreCase(upstreamRepositoryName)) {

            context.getVcsHostingService()
                   .getUserFork(context.getHostUserLogin(), upstreamRepositoryOwner, upstreamRepositoryName)
                   .then(new Operation<Repository>() {
                       @Override
                       public void apply(Repository fork) throws OperationException {
                           proceed(fork.getName(), executor, context);
                       }
                   })
                   .catchError(new Operation<PromiseError>() {
                       @Override
                       public void apply(PromiseError error) throws OperationException {
                           if (error.getCause() instanceof NoUserForkException) {
                               createFork(executor, context, upstreamRepositoryOwner, upstreamRepositoryName);
                               return;
                           }

                           executor.fail(CreateForkStep.this, context, error.getCause().getMessage());
                       }
                   });
        } else {
            // user fork has been cloned
            proceed(originRepositoryName, executor, context);
        }
    }

    private void createFork(final WorkflowExecutor executor,
                            final Context context,
                            final String upstreamRepositoryOwner,
                            final String upstreamRepositoryName) {
        context.getVcsHostingService()
               .fork(upstreamRepositoryOwner, upstreamRepositoryName)
               .then(new Operation<Repository>() {
                   @Override
                   public void apply(Repository result) throws OperationException {
                       proceed(result.getName(), executor, context);
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError err) throws OperationException {
                       final String errorMessage = messages.stepCreateForkErrorCreatingFork(upstreamRepositoryOwner,
                                                                                            upstreamRepositoryName,
                                                                                            err.getCause().getMessage());
                       executor.fail(CreateForkStep.this, context, errorMessage);
                   }
               });
    }

    private void proceed(final String forkName, final WorkflowExecutor executor, final Context context) {
        context.setForkedRepositoryName(forkName);
        executor.done(this, context);
    }
}
