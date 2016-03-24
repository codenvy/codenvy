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

/**
 * Defines contribution workflow.
 *
 * <p>The contribution workflow consists of 3 main steps:
 * initialization, pull request creation, pull request update.
 * According to these 3 steps implementation should provide steps chains
 * based on specific VCS Hosting service.
 *
 * <p>Binding example:
 * <pre>{@code
 *     final GinMapBinder<String, ContributionWorkflow> strategyBinder
 *                 = GinMapBinder.newMapBinder(binder(), String.class, ContributionWorkflow.class);
 *     strategyBinder.addBinding("my-vcs-service").to(MyVcsServiceContributionWorkflow.class);
 * }</pre>
 *
 * @author Yevhenii Voevodin
 */
public interface ContributionWorkflow {

    /** Returns the steps chain which should be executed when plugin initializes. */
    StepsChain initChain(Context context);

    /** Returns the steps chain which should be executed when pull request should be created. */
    StepsChain creationChain(Context context);

    /** Returns the steps chain which should be executed for the pull request updatePullRequest. */
    StepsChain updateChain(Context context);
}
