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
 * Defines workflow status contract.
 *
 * @author Yevhenii Voevodin
 */
public enum WorkflowStatus {

    /**
     * The workflow is in initializing status only and only if chain executor
     * is performing {@link ContributionWorkflow#initChain(Context)} steps.
     * If workflow is not initialized properly then {@link WorkflowExecutor#invalidateContext(Context)}
     * method should be performed which will invalidate context and remove initializing status.
     *
     * <p>The status map:
     * <pre>
     *      INITIALIZING -> READY_TO_CREATE_PR (normal)
     *      INITIALIZING -> READY_TO_UPDATE (if pr exists)
     *      INITIALIZING -> executor#invalidateContext (if an error occurs)
     * </pre>
     */
    INITIALIZING,

    /**
     * The workflow is ready to createPr when either initialization is successfully performed
     * or {@link #CREATING_PR} failed with an error.
     *
     * <p>The status map:
     * <pre>
     *     INITIALIZING        -> READY_TO_CREATE_PR -> CONTRIBUTING (normal)
     *     READY_TO_CREATE_PR  -> CONTRIBUTING       -> READY_TO_CREATE_PR (if an error occurs)
     * </pre>
     */
    READY_TO_CREATE_PR,

    /**
     * The workflow is in creating pr status status only when chain executor is
     * performing {@link ContributionWorkflow#creationChain(Context)}.
     * If any error occurs during pr creating then workflow status should be
     * changed either to the {@link #READY_TO_CREATE_PR} or to the {@link #READY_TO_UPDATE_PR}
     *
     * <p>The status map:
     * <pre>
     *     READY_TO_CREATE_PR -> CREATING_PR -> READY_TO_UPDATE_PR (when successfully updated || pr already exists)
     *     READY_TO_CREATE_PR -> CREATING_PR -> READY_TO_CREATE_PR (if an error occurs)
     * </pre>
     */
    CREATING_PR,

    /**
     * The workflow is ready to updatePullRequest pr either when plugin
     * was initialized successfully and pull request already
     * exists, or if pull request was successfully created.
     *
     * <p>The status map:
     * <pre>
     *     CREATING_PR  -> READY_TO_UPDATE -> UPDATING_PR (normal || pr already exists)
     *     INITIALIZING -> READY_TO_UPDATE -> UPDATING_PR (if pr exists)
     * </pre>
     */
    READY_TO_UPDATE_PR,

    /**
     * The workflow is updating pull request only and only if chain executor
     * executes {@link ContributionWorkflow#updateChain(Context)}.
     * If an error occurs during the updatePullRequest, status should be changed to {@link #READY_TO_UPDATE_PR}.
     *
     * <p>The status map:
     * <pre>
     *     READY_TO_UPDATE -> UPDATING_PR -> READY_TO_UPDATE (normal || an error occurs)
     * </pre>
     */
    UPDATING_PR
}
