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
package com.codenvy.plugin.pullrequest.client.workflow;

import com.codenvy.plugin.pullrequest.client.events.StepEvent;
import com.codenvy.plugin.pullrequest.client.steps.PushBranchStep;

/**
 * Contract for a step in the contribution workflow.
 *
 * <p>Step should not depend on another steps as it is
 * a workflow part but workflow is defined by {@link ContributionWorkflow} implementations.
 * Each step should end its execution with either {@link WorkflowExecutor#done(Step, Context)}
 * or {@link WorkflowExecutor#fail(Step, Context, String)} method.
 *
 * <p>{@link WorkflowExecutor} fires {@link StepEvent} for each
 * done/fail step if this step is not {@link SyntheticStep}.
 *
 * <p>If step contains common logic for several steps
 * then this logic should be either extracted to the other
 * step or used along with factory (e.g. {@link PushBranchStep}).
 *
 * @author Yevhenii Voevodin
 */
public interface Step {

    /**
     * Executes this step.
     *
     * @param executor
     *         contribution workflow executor
     */
    void execute(final WorkflowExecutor executor, final Context context);
}
