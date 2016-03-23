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
package com.codenvy.plugin.pullrequest.client.vcs.hosting;

import javax.validation.constraints.NotNull;

/**
 * Exception raised when a pull request already exists for a branch.
 *
 * @author Kevin Pollet
 */
public class PullRequestAlreadyExistsException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an instance of {@link PullRequestAlreadyExistsException}.
     *
     * @param headBranch
     *         the head branch name.
     */
    public PullRequestAlreadyExistsException(@NotNull final String headBranch) {
        super("A pull request for " + headBranch + " already exists");
    }
}
