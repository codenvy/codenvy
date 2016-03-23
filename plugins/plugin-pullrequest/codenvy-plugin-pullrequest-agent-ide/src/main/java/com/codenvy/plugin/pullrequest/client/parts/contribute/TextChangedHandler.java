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

/**
 * Used to detect if pull request title/comment/branch is changed.
 *
 * @author Yevhenii Voevodin
 * @see ContributePartView#addBranchChangedHandler(TextChangedHandler)
 * @see ContributePartView#addContributionCommentChangedHandler(TextChangedHandler)
 * @see ContributePartView#addContributionTitleChangedHandler(TextChangedHandler)
 */
public interface TextChangedHandler {

    /**
     * Called when title/comment/branch is changed
     *
     * @param newText
     *         new text content
     */
    void onTextChanged(String newText);
}
