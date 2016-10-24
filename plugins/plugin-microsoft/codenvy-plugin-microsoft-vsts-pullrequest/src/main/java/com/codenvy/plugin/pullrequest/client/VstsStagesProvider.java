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
package com.codenvy.plugin.pullrequest.client;

import com.codenvy.plugin.pullrequest.client.parts.contribute.StagesProvider;
import com.codenvy.plugin.pullrequest.client.steps.CheckBranchToPush;
import com.codenvy.plugin.pullrequest.client.steps.CommitWorkingTreeStep;
import com.codenvy.plugin.pullrequest.client.steps.DetectPullRequestStep;
import com.codenvy.plugin.pullrequest.client.steps.IssuePullRequestStep;
import com.codenvy.plugin.pullrequest.client.steps.PushBranchOnOriginStep;
import com.codenvy.plugin.pullrequest.client.steps.UpdatePullRequestStep;
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Provides displayed stages for Visual Studio Team Services contribution workflow.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class VstsStagesProvider implements StagesProvider {

    private static final Set<Class<? extends Step>> DONE_STEP_TYPES;
    private static final Set<Class<? extends Step>> ERROR_STEP_TYPES;

    static {
        DONE_STEP_TYPES = ImmutableSet.of(IssuePullRequestStep.class,
                                          PushBranchOnOriginStep.class,
                                          UpdatePullRequestStep.class);
        ERROR_STEP_TYPES = ImmutableSet.of(IssuePullRequestStep.class,
                                           PushBranchOnOriginStep.class,
                                           UpdatePullRequestStep.class,
                                           CheckBranchToPush.class);
    }

    private final ContributeMessages messages;

    @Inject
    public VstsStagesProvider(ContributeMessages messages) {
        this.messages = messages;
    }

    @Override
    public List<String> getStages(Context context) {
        return asList(messages.contributePartStatusSectionBranchPushedOriginStepLabel(),
                      messages.contributePartStatusSectionPullRequestIssuedStepLabel());
    }

    @Override
    public Set<Class<? extends Step>> getStepDoneTypes(Context context) {
        return DONE_STEP_TYPES;
    }

    @Override
    public Set<Class<? extends Step>> getStepErrorTypes(Context context) {
        return ERROR_STEP_TYPES;
    }

    @Override
    public Class<? extends Step> getDisplayStagesType(Context context) {
        return context.isUpdateMode() ? CommitWorkingTreeStep.class : DetectPullRequestStep.class;
    }
}
