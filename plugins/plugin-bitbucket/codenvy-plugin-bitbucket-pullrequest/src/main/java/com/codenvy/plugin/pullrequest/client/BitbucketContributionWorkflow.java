/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.plugin.pullrequest.client;

import org.eclipse.che.plugin.pullrequest.client.steps.AddHttpForkRemoteStep;
import org.eclipse.che.plugin.pullrequest.client.steps.AddReviewFactoryLinkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.AddSshForkRemoteStep;
import org.eclipse.che.plugin.pullrequest.client.steps.AuthorizeCodenvyOnVCSHostStep;
import org.eclipse.che.plugin.pullrequest.client.steps.CommitWorkingTreeStep;
import org.eclipse.che.plugin.pullrequest.client.steps.CreateForkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DefineExecutionConfiguration;
import org.eclipse.che.plugin.pullrequest.client.steps.DefineWorkBranchStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DetectPullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DetermineUpstreamRepositoryStep;
import org.eclipse.che.plugin.pullrequest.client.steps.GenerateReviewFactoryStep;
import org.eclipse.che.plugin.pullrequest.client.steps.InitializeWorkflowContextStep;
import org.eclipse.che.plugin.pullrequest.client.steps.IssuePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchOnForkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchOnOriginStep;
import org.eclipse.che.plugin.pullrequest.client.steps.UpdatePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.ContributionWorkflow;
import org.eclipse.che.plugin.pullrequest.client.workflow.StepsChain;
import com.google.common.base.Supplier;

import javax.inject.Inject;

/**
 * Declares steps of contribution workflow for Bitbucket repositories.
 *
 * @author Mihail Kuznyetsov
 */
public class BitbucketContributionWorkflow implements ContributionWorkflow {

    private static final String BITBUCKET_HOSTED_ENDPOINT = "https://bitbucket.org";

    private final InitializeWorkflowContextStep   initializeWorkflowContextStep;
    private final DefineWorkBranchStep            defineWorkBranchStep;
    private final CommitWorkingTreeStep           commitWorkingTreeStep;
    private final AuthorizeCodenvyOnVCSHostStep   authorizeCodenvyOnVCSHostStep;
    private final DefineExecutionConfiguration    defineExecutionConfiguration;
    private final DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep;
    private final CreateForkStep                  createForkStep;
    private final AddHttpForkRemoteStep           addHttpForkRemoteStep;
    private final AddSshForkRemoteStep            addSshForkRemoteStep;
    private final PushBranchOnForkStep            pushBranchOnForkStep;
    private final PushBranchOnOriginStep          pushBranchOnOriginStep;
    private final GenerateReviewFactoryStep       generateReviewFactoryStep;
    private final AddReviewFactoryLinkStep        addReviewFactoryLinkStep;
    private final IssuePullRequestStep            issuePullRequestStep;
    private final UpdatePullRequestStep           updatePullRequestStep;
    private final DetectPullRequestStep           detectPullRequestStep;


    @Inject
    public BitbucketContributionWorkflow(InitializeWorkflowContextStep initializeWorkflowContextStep,
                                         DefineWorkBranchStep defineWorkBranchStep,
                                         CommitWorkingTreeStep commitWorkingTreeStep,
                                         AuthorizeCodenvyOnVCSHostStep authorizeCodenvyOnVCSHostStep,
                                         DefineExecutionConfiguration defineExecutionConfiguration,
                                         DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep,
                                         DetectPullRequestStep detectPullRequestStep,
                                         CreateForkStep createForkStep,
                                         AddHttpForkRemoteStep addHttpForkRemoteStep,
                                         AddSshForkRemoteStep addSshForkRemoteStep,
                                         PushBranchOnForkStep pushBranchOnForkStep,
                                         PushBranchOnOriginStep pushBranchOnOriginStep,
                                         GenerateReviewFactoryStep generateReviewFactoryStep,
                                         AddReviewFactoryLinkStep addReviewFactoryLinkStep,
                                         IssuePullRequestStep issuePullRequestStep,
                                         UpdatePullRequestStep updatePullRequestStep) {
        this.initializeWorkflowContextStep = initializeWorkflowContextStep;
        this.defineWorkBranchStep = defineWorkBranchStep;
        this.commitWorkingTreeStep = commitWorkingTreeStep;
        this.authorizeCodenvyOnVCSHostStep = authorizeCodenvyOnVCSHostStep;
        this.defineExecutionConfiguration = defineExecutionConfiguration;
        this.determineUpstreamRepositoryStep = determineUpstreamRepositoryStep;
        this.detectPullRequestStep = detectPullRequestStep;
        this.createForkStep = createForkStep;
        this.addHttpForkRemoteStep = addHttpForkRemoteStep;
        this.addSshForkRemoteStep = addSshForkRemoteStep;
        this.pushBranchOnForkStep = pushBranchOnForkStep;
        this.pushBranchOnOriginStep = pushBranchOnOriginStep;
        this.generateReviewFactoryStep = generateReviewFactoryStep;
        this.addReviewFactoryLinkStep = addReviewFactoryLinkStep;
        this.issuePullRequestStep = issuePullRequestStep;
        this.updatePullRequestStep = updatePullRequestStep;

    }

    @Override
    public StepsChain initChain(Context context) {
        return StepsChain.first(initializeWorkflowContextStep)
                         .then(defineWorkBranchStep);
    }

    @Override
    public StepsChain creationChain(final Context context) {
        return StepsChain.first(commitWorkingTreeStep)
                         .then(authorizeCodenvyOnVCSHostStep)
                         .then(defineExecutionConfiguration)
                         .then(determineUpstreamRepositoryStep)
                         .then(detectPullRequestStep)
                         .thenChainIf(new Supplier<Boolean>() {
                                          @Override
                                          public Boolean get() {
                                              return context.isForkAvailable();
                                          }
                                      },
                                      StepsChain.first(createForkStep)
                                                .then(BITBUCKET_HOSTED_ENDPOINT.equals(context.getVcsHostingService().getHost())
                                                      ? addHttpForkRemoteStep : addSshForkRemoteStep)
                                                .then(pushBranchOnForkStep),
                                      StepsChain.first(pushBranchOnOriginStep))
                         .then(generateReviewFactoryStep)
                         .thenIf(new Supplier<Boolean>() {
                             @Override
                             public Boolean get() {
                                 return context.getReviewFactoryUrl() != null;
                             }
                         }, addReviewFactoryLinkStep)
                         .then(issuePullRequestStep);
    }

    @Override
    public StepsChain updateChain(final Context context) {
        return StepsChain.first(commitWorkingTreeStep)
                         .then(authorizeCodenvyOnVCSHostStep)
                         .then(defineExecutionConfiguration)
                         .thenChainIf(new Supplier<Boolean>() {
                                          @Override
                                          public Boolean get() {
                                              return context.isForkAvailable();
                                          }
                                      },
                                      StepsChain.first(BITBUCKET_HOSTED_ENDPOINT.equals(context.getVcsHostingService().getHost())
                                                       ? addHttpForkRemoteStep : addSshForkRemoteStep)
                                                .then(pushBranchOnForkStep),
                                      StepsChain.first(pushBranchOnOriginStep))
                         .then(updatePullRequestStep);
    }
}
