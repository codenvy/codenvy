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
package com.codenvy.plugin.pullrequest.client.parts.contribute;

import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingService;

import java.util.List;
import java.util.Set;

/**
 * Provider should be implemented one per {@link VcsHostingService}.
 *
 * <p>Binding example:
 * <pre>{@code
 *      final GinMapBinder<String, StagesProvider> stagesProvider
 *              = GinMapBinder.newMapBinder(binder(),
 *                                          String.class,
 *                                          StagesProvider.class);
 *      stagesProvider.addBinding(GitHubHostingService.SERVICE_NAME).to(GithubStagesProvider.class);
 * }
 * </pre>
 *
 * @author Yevhenii Voevodin
 */
public interface StagesProvider {

    /**
     * Returns the list of stages which should be displayed
     * when pull request update or creation starts.
     *
     * @param context
     *         current execution context
     * @return the list of stages
     */
    List<String> getStages(final Context context);

    /**
     * When step is done and its class is result of this method
     * then current stage is considered as successfully done.
     *
     * @param context
     *         current execution context
     * @return react classes
     */
    Set<Class<? extends Step>> getStepDoneTypes(final Context context);


    /**
     * When step is done with an error and its class is result of this method
     * then current stage is considered as successfully done.
     *
     * @param context
     *         current execution context
     * @return error react classes
     */
    Set<Class<? extends Step>> getStepErrorTypes(final Context context);

    /**
     * Stages are shown only once and the time to show stages is defined
     * by return type of this method. If that step(which type is returned) is
     * successfully executed then {@link #getStages(Context)} method will be used
     * to show the stages. It is needed for dynamic stages list detection
     * (e.g. when workflow configures context in create/update chains).
     *
     * @param context
     *         current execution context
     * @return returns step class after which successful execution stages should be shown
     */
    Class<? extends Step> getDisplayStagesType(final Context context);
}
