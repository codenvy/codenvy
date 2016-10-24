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

import com.codenvy.plugin.pullrequest.client.utils.FactoryHelper;
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.pullrequest.shared.dto.Configuration;
import com.codenvy.plugin.pullrequest.shared.dto.PullRequest;
import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.factory.FactoryServiceClient;

/**
 * Used for toggling last step mark.
 *
 * @author Yevhenii Voevodin
 * @author Anton Korneta
 */
@Singleton
public class UpdatePullRequestStep implements Step {
    private static final RegExp SCHEMA = RegExp.compile("[^\\.]*(f\\?id=)([a-zA-Z0-9]*)[^\\.]*");

    private final FactoryServiceClient factoryService;
    private final AppContext           appContext;

    @Inject
    public UpdatePullRequestStep(FactoryServiceClient factoryService,
                                 AppContext appContext) {
        this.factoryService = factoryService;
        this.appContext = appContext;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        final PullRequest pullRequest = context.getPullRequest();
        factoryService.getFactoryJson(appContext.getWorkspaceId(), null)
                      .then(new Operation<FactoryDto>() {
                          @Override
                          public void apply(FactoryDto currentFactory) throws OperationException {
                              final String factoryId = extractFactoryId(pullRequest.getDescription());
                              if (!Strings.isNullOrEmpty(factoryId)) {
                                  updateFactory(executor, context, factoryId, currentFactory);
                              } else {
                                  addReviewUrl(executor, context, currentFactory);
                              }
                          }
                      })
                      .catchError(handleError(executor, context));
    }

    private Promise<FactoryDto> updateFactory(final WorkflowExecutor executor,
                                           final Context context,
                                           final String factoryId,
                                           final FactoryDto currentFactory) {
        return factoryService.updateFactory(factoryId, currentFactory)
                             .then(new Operation<FactoryDto>() {
                                 @Override
                                 public void apply(FactoryDto updatedFactory) throws OperationException {
                                     context.setReviewFactoryUrl(FactoryHelper.getAcceptFactoryUrl(updatedFactory));
                                     executor.done(UpdatePullRequestStep.this, context);
                                 }
                             })
                             .catchError(new Operation<PromiseError>() {
                                 @Override
                                 public void apply(PromiseError error) throws OperationException {
                                     createNewFactory(executor,
                                                      context,
                                                      currentFactory,
                                                      new Operation<FactoryDto>() {
                                                          @Override
                                                          public void apply(FactoryDto factory) throws OperationException {
                                                              final PullRequest pull = context.getPullRequest();
                                                              doUpdate(executor,
                                                                       context,
                                                                       pull,
                                                                       pull.getDescription().replaceAll(factoryId, factory.getId()));
                                                          }
                                                      });
                                 }
                             });
    }

    private void addReviewUrl(final WorkflowExecutor executor,
                              final Context context,
                              final FactoryDto currentFactory) {
        createNewFactory(executor,
                         context,
                         currentFactory,
                         new Operation<FactoryDto>() {
                             @Override
                             public void apply(FactoryDto factory) throws OperationException {
                                 final Configuration configuration = context.getConfiguration();
                                 final String reviewUrl = context.getVcsHostingService()
                                                                 .formatReviewFactoryUrl(FactoryHelper.getAcceptFactoryUrl(factory));
                                 context.setReviewFactoryUrl(reviewUrl);
                                 final String comment = reviewUrl + "\n" + configuration.getContributionComment();
                                 configuration.withContributionComment(comment);
                                 doUpdate(executor, context, context.getPullRequest(), comment);
                             }
                         });
    }


    private void createNewFactory(final WorkflowExecutor executor,
                                  final Context context,
                                  final FactoryDto factory,
                                  Operation<FactoryDto> operation) {
        factoryService.saveFactory(factory).then(operation).catchError(handleError(executor, context));
    }

    private void doUpdate(final WorkflowExecutor executor,
                          final Context context,
                          final PullRequest pullRequest,
                          final String comment) {
        context.getVcsHostingService()
               .updatePullRequest(context.getOriginRepositoryOwner(),
                                  context.getUpstreamRepositoryName(),
                                  pullRequest.withDescription(comment))
               .then(new Operation<PullRequest>() {
                   @Override
                   public void apply(PullRequest pr) throws OperationException {
                       executor.done(UpdatePullRequestStep.this, context);
                   }
               })
               .catchError(handleError(executor, context));
    }

    private String extractFactoryId(String description) {
        if (SCHEMA.test(description)) {
            return SCHEMA.exec(description).getGroup(2);
        }
        return null;
    }

    private Operation<PromiseError> handleError(final WorkflowExecutor executor, final Context context) {
        return new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError err) throws OperationException {
                context.getViewState().setStatusMessage(err.getMessage(), true);
                executor.fail(UpdatePullRequestStep.this, context, err.getMessage());
            }
        };
    }
}
