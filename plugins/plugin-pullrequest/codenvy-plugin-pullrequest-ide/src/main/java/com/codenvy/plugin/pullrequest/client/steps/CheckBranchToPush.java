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
import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Checks that working branch is different from the cloned branch.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class CheckBranchToPush implements Step {

    private final ContributeMessages messages;

    @Inject
    public CheckBranchToPush(final ContributeMessages messages) {
        this.messages = messages;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        if (context.getWorkBranchName().equals(context.getContributeToBranchName())) {
            executor.fail(this,
                          context,
                          messages.stepCheckBranchClonedBranchIsEqualToWorkBranch());
        } else {
            executor.done(this, context);
        }
    }
}
