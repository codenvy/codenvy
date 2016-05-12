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
import com.codenvy.plugin.pullrequest.client.utils.FactoryHelper;
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Singleton;

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.factory.FactoryServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;

import javax.inject.Inject;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Generates a factory for the contribution reviewer.
 */
@Singleton
public class GenerateReviewFactoryStep implements Step {
    private final ContributeMessages   messages;
    private final AppContext           appContext;
    private final NotificationManager  notificationManager;
    private final FactoryServiceClient factoryService;

    @Inject
    public GenerateReviewFactoryStep(final ContributeMessages messages,
                                     final AppContext appContext,
                                     final NotificationManager notificationManager,
                                     final FactoryServiceClient factoryService) {
        this.messages = messages;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.factoryService = factoryService;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        factoryService.getFactoryJson(appContext.getWorkspaceId(), null)
                      .then(updateProjectAttributes(context))
                      .then(new Operation<Factory>() {
                          @Override
                          public void apply(Factory factory) throws OperationException {
                              factoryService.saveFactory(factory)
                                            .then(new Operation<Factory>() {
                                                @Override
                                                public void apply(Factory factory) throws OperationException {
                                                    context.setReviewFactoryUrl(FactoryHelper.getAcceptFactoryUrl(factory));
                                                    executor.done(GenerateReviewFactoryStep.this, context);
                                                }
                                            })
                                            .catchError(new Operation<PromiseError>() {
                                                @Override
                                                public void apply(PromiseError arg) throws OperationException {
                                                    notificationManager.notify(messages.stepGenerateReviewFactoryErrorCreateFactory(),
                                                                               FAIL,
                                                                               NOT_EMERGE_MODE);
                                                    executor.done(GenerateReviewFactoryStep.this, context);
                                                }
                                            });
                          }
                      })
                      .catchError(new Operation<PromiseError>() {
                          @Override
                          public void apply(PromiseError arg) throws OperationException {
                              notificationManager.notify(messages.stepGenerateReviewFactoryErrorCreateFactory(),
                                                         FAIL,
                                                         NOT_EMERGE_MODE);
                              executor.done(GenerateReviewFactoryStep.this, context);
                          }
                      });
    }

    private Function<Factory, Factory> updateProjectAttributes(final Context context) {
        return new Function<Factory, Factory>() {
            @Override
            public Factory apply(Factory factory) throws FunctionException {
                final Optional<ProjectConfigDto> projectOpt = FluentIterable.from(factory.getWorkspace().getProjects())
                                                                            .filter(new Predicate<ProjectConfigDto>() {
                                                                                @Override
                                                                                public boolean apply(ProjectConfigDto project) {
                                                                                    return project.getName()
                                                                                                  .equals(context.getProject().getName());
                                                                                }
                                                                            }).first();
                if (projectOpt.isPresent()) {
                    final ProjectConfigDto project = projectOpt.get();
                    project.getSource().getParameters().put("branch", context.getWorkBranchName());

                    if (context.isForkAvailable()) {
                        project.getSource().setLocation(context.getVcsHostingService()
                               .makeHttpRemoteUrl(context.getHostUserLogin(), context.getOriginRepositoryName()));
                    }
                }
                return factory;
            }
        };
    }
}
