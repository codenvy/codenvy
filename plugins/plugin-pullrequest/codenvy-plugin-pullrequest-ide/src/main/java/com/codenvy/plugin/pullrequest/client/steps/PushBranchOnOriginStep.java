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

import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.inject.Singleton;

import javax.inject.Inject;

/**
 * Push the local contribution branch to origin repository
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class PushBranchOnOriginStep implements Step {

    private final PushBranchStepFactory pushBranchStepFactory;

    @Inject
    public PushBranchOnOriginStep(PushBranchStepFactory pushBranchStepFactory) {
        this.pushBranchStepFactory = pushBranchStepFactory;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        pushBranchStepFactory.create(this,
                                     context.getOriginRepositoryOwner(),
                                     context.getOriginRepositoryName())
                             .execute(executor, context);
    }
}
