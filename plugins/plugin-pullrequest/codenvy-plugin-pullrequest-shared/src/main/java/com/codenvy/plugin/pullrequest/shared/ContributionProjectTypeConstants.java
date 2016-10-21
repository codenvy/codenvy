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
package com.codenvy.plugin.pullrequest.shared;

/**
 * Shared constants for the contribution project type.
 *
 * @author Kevin Pollet
 */
public final class ContributionProjectTypeConstants {
    public static final String CONTRIBUTION_PROJECT_TYPE_ID = "pullrequest";

    public static final String CONTRIBUTION_PROJECT_TYPE_DISPLAY_NAME = "contribution";

    /** Contribution mode variable used to name the local branch that is initialized. */
    public static final String CONTRIBUTE_LOCAL_BRANCH_NAME = "local_branch";

    /** Contribution mode variable used to know in which branch the contribution has to be pushed. */
    public static final String CONTRIBUTE_TO_BRANCH_VARIABLE_NAME = "contribute_to_branch";

    /**
     * Disable instantiation.
     */
    private ContributionProjectTypeConstants() {
    }
}
