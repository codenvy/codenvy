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

import com.google.common.base.Optional;

import org.eclipse.che.ide.util.loging.Log;

import java.util.Iterator;

import static com.google.common.base.Optional.fromNullable;
import static java.util.Objects.requireNonNull;

/**
 * Executor for the {@link StepsChain}.
 * If the chain is modified after executor is created (e.g. new step added to the chain)
 * executor state won't be affected and newly added steps will be ignored by executor.
 *
 * @author Yevhenii Voevodin
 */
public final class ChainExecutor {

    private final Iterator<Step> chainIt;

    private Step currentStep;

    public ChainExecutor(final StepsChain chain) {
        chainIt = requireNonNull(chain, "Expected non-null steps chain").getSteps().iterator();
    }

    /**
     * Executes the next chain step, does nothing - if there are no steps left .
     *
     * @param workflow
     *         the contribution workflow
     * @param context
     *         the context for current chain execution
     */
    public void execute(final WorkflowExecutor workflow, final Context context) {
        if (chainIt.hasNext()) {
            currentStep = chainIt.next();
            Log.info(getClass(), "Executing :: " + context.getProject().getName() + " ::  =>  " + currentStep.getClass());
            currentStep.execute(workflow, context);
        }
    }

    /**
     * Returns an empty optional when current step is null, otherwise
     * returns the optional which contains current step value.
     */
    public Optional<Step> getCurrentStep() {
        return fromNullable(currentStep);
    }
}
