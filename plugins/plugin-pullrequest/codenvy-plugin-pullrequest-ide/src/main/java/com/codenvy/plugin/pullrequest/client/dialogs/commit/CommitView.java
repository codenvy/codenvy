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
package com.codenvy.plugin.pullrequest.client.dialogs.commit;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;

/**
 * View for committing uncommitted project changes.
 *
 * @author Kevin Pollet
 */
public interface CommitView extends View<CommitView.ActionDelegate> {
    /**
     * Opens the commit view with the given commit description.
     */
    void show(String commitDescription);

    /**
     * Close the commit view.
     */
    void close();

    /**
     * Returns the current commit description.
     *
     * @return the current commit description.
     */
    @NotNull
    String getCommitDescription();

    /**
     * Enables or disables the button OK.
     *
     * @param enabled
     *         {@code true} to enable the OK button, {@code false} otherwise.
     */
    void setOkButtonEnabled(final boolean enabled);

    /**
     * Returns if the untracked files must be added.
     *
     * @return {@code true} if untracked files must be added, {@code false} otherwise.
     */
    boolean isIncludeUntracked();

    /**
     * The action delegate.
     */
    interface ActionDelegate {
        /**
         * Called when project changes must be committed.
         */
        void onOk();

        /**
         * Called when project changes must not be committed.
         */
        void onContinue();

        /**
         * Called when the operation must be aborted.
         */
        void onCancel();

        /**
         * Called when the commit description is changed.
         */
        void onCommitDescriptionChanged();
    }
}
