/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.plugin.pullrequest.client;

import com.google.common.base.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.plugin.pullrequest.client.steps.AddReviewFactoryLinkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.AuthorizeCodenvyOnVCSHostStep;
import org.eclipse.che.plugin.pullrequest.client.steps.CheckBranchToPush;
import org.eclipse.che.plugin.pullrequest.client.steps.CommitWorkingTreeStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DefineWorkBranchStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DetectPullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DetermineUpstreamRepositoryStep;
import org.eclipse.che.plugin.pullrequest.client.steps.GenerateReviewFactoryStep;
import org.eclipse.che.plugin.pullrequest.client.steps.InitializeWorkflowContextStep;
import org.eclipse.che.plugin.pullrequest.client.steps.IssuePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchOnOriginStep;
import org.eclipse.che.plugin.pullrequest.client.steps.UpdatePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.ContributionWorkflow;
import org.eclipse.che.plugin.pullrequest.client.workflow.StepsChain;

@Singleton
/**
 * Declares steps of contribution workflow for Visual Studio Team Services repositories.
 *
 * @author Yevhenii Voevodin
 */
public class VstsContributionWorkflow implements ContributionWorkflow {

  private final InitializeWorkflowContextStep initializeWorkflowContextStep;
  private final DefineWorkBranchStep defineWorkBranchStep;
  private final CommitWorkingTreeStep commitWorkingTreeStep;
  private final AuthorizeCodenvyOnVCSHostStep authorizeCodenvyOnVCSHostStep;
  private final DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep;
  private final PushBranchOnOriginStep pushBranchOnOriginStep;
  private final GenerateReviewFactoryStep generateReviewFactoryStep;
  private final AddReviewFactoryLinkStep addReviewFactoryLinkStep;
  private final IssuePullRequestStep issuePullRequestStep;
  private final UpdatePullRequestStep updatePullRequestStep;
  private final CheckBranchToPush checkBranchToPushIsDifferentFromClonedBranchStep;
  private final DetectPullRequestStep detectPullRequestStep;

  @Inject
  public VstsContributionWorkflow(
      InitializeWorkflowContextStep initializeWorkflowContextStep,
      DefineWorkBranchStep defineWorkBranchStep,
      CommitWorkingTreeStep commitWorkingTreeStep,
      AuthorizeCodenvyOnVCSHostStep authorizeCodenvyOnVCSHostStep,
      DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep,
      PushBranchOnOriginStep pushBranchOnOriginStep,
      GenerateReviewFactoryStep generateReviewFactoryStep,
      AddReviewFactoryLinkStep addReviewFactoryLinkStep,
      IssuePullRequestStep issuePullRequestStep,
      UpdatePullRequestStep updatePullRequestStep,
      CheckBranchToPush checkBranchToPushIsDifferentFromClonedBranchStep,
      DetectPullRequestStep detectPullRequestStep) {
    this.initializeWorkflowContextStep = initializeWorkflowContextStep;
    this.defineWorkBranchStep = defineWorkBranchStep;
    this.commitWorkingTreeStep = commitWorkingTreeStep;
    this.authorizeCodenvyOnVCSHostStep = authorizeCodenvyOnVCSHostStep;
    this.determineUpstreamRepositoryStep = determineUpstreamRepositoryStep;
    this.pushBranchOnOriginStep = pushBranchOnOriginStep;
    this.generateReviewFactoryStep = generateReviewFactoryStep;
    this.addReviewFactoryLinkStep = addReviewFactoryLinkStep;
    this.issuePullRequestStep = issuePullRequestStep;
    this.updatePullRequestStep = updatePullRequestStep;
    this.checkBranchToPushIsDifferentFromClonedBranchStep =
        checkBranchToPushIsDifferentFromClonedBranchStep;
    this.detectPullRequestStep = detectPullRequestStep;
  }

  @Override
  public StepsChain initChain(final Context context) {
    return StepsChain.first(initializeWorkflowContextStep).then(defineWorkBranchStep);
  }

  @Override
  public StepsChain creationChain(final Context context) {
    return StepsChain.first(commitWorkingTreeStep)
        .then(checkBranchToPushIsDifferentFromClonedBranchStep)
        .then(authorizeCodenvyOnVCSHostStep)
        .then(determineUpstreamRepositoryStep)
        .then(detectPullRequestStep)
        .then(pushBranchOnOriginStep)
        .then(generateReviewFactoryStep)
        .thenIf(
            new Supplier<Boolean>() {
              @Override
              public Boolean get() {
                return context.getReviewFactoryUrl() != null;
              }
            },
            addReviewFactoryLinkStep)
        .then(issuePullRequestStep);
  }

  @Override
  public StepsChain updateChain(final Context context) {
    return StepsChain.first(commitWorkingTreeStep)
        .then(authorizeCodenvyOnVCSHostStep)
        .then(pushBranchOnOriginStep)
        .then(updatePullRequestStep);
  }
}
