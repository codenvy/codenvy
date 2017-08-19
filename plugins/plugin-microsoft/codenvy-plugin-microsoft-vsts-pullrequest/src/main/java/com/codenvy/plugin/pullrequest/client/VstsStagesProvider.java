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

import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.parts.contribute.StagesProvider;
import org.eclipse.che.plugin.pullrequest.client.steps.CheckBranchToPush;
import org.eclipse.che.plugin.pullrequest.client.steps.CommitWorkingTreeStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DetectPullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.IssuePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchOnOriginStep;
import org.eclipse.che.plugin.pullrequest.client.steps.UpdatePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;

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
    DONE_STEP_TYPES =
        ImmutableSet.of(
            IssuePullRequestStep.class, PushBranchOnOriginStep.class, UpdatePullRequestStep.class);
    ERROR_STEP_TYPES =
        ImmutableSet.of(
            IssuePullRequestStep.class,
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
    return asList(
        messages.contributePartStatusSectionBranchPushedOriginStepLabel(),
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
