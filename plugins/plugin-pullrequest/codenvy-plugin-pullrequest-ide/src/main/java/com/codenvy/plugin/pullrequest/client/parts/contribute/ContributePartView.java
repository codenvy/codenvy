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

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

import java.util.List;

/**
 * Interface for the contribution configuration shown when the user decides to send their contribution.
 */
public interface ContributePartView extends View<ContributePartView.ActionDelegate> {
    /**
     * Set factory's repository URL.
     */
    void setRepositoryUrl(String url);

    /**
     * Set factory's contribute to branch name.
     */
    void setContributeToBranch(String branch);

    /**
     * Set project name.
     */
    void setProjectName(String projectName);

    /**
     * Returns the contribution branch name.
     *
     * @return the contribution branch name
     */
    String getContributionBranchName();

    /**
     * Sets the contribution branch name.
     *
     * @param branchName
     *         the contribution branch name.
     */
    void setContributionBranchName(String branchName);

    /**
     * Set the contribution branch name list.
     *
     * @param branchNames
     *         the branch name list.
     */
    void setContributionBranchNameList(List<String> branchNames);

    /**
     * Sets the enabled/disabled state of the contribution branch name field.
     */
    void setContributionBranchNameEnabled(boolean enabled);

    /**
     * Returns the current content of the contribution comment.
     *
     * @return the comment.
     */
    String getContributionComment();

    /**
     * Sets the contribution comment.
     *
     * @param comment
     *         the contribution comment.
     */
    void setContributionComment(String comment);

    void addContributionCommentChangedHandler(TextChangedHandler handler);

    /**
     * Sets the enabled/disabled state of the contribution comment field.
     */
    void setContributionCommentEnabled(boolean enabled);

    /**
     * Returns the contribution title.
     *
     * @return the title.
     */
    String getContributionTitle();

    /**
     * Sets the contribution title.
     *
     * @param title
     *         the contribution title.
     */
    void setContributionTitle(String title);

    void addContributionTitleChangedHandler(TextChangedHandler handler);

    void addBranchChangedHandler(TextChangedHandler changeHandler);

    /**
     * Sets the enabled/disabled state of the contribution title field.
     */
    void setContributionTitleEnabled(boolean enabled);

    /**
     * Sets the contribution title input error state.
     *
     * @param showError
     *         {@code true} if the contribution title is in error, {@code false} otherwise.
     */
    void showContributionTitleError(boolean showError);

    /**
     * Sets the enabled/disabled state of the "Contribute" button.
     *
     * @param enabled
     *         true to enable, false to disable
     */
    void setContributeButtonEnabled(boolean enabled);

    /**
     * Sets the text displayed into the "Contribute" button.
     *
     * @param text
     *         the text to display
     */
    void setContributeButtonText(String text);

    /**
     * Shows the status section.
     */
    void showStatusSection(String... statusSteps);

    /**
     * Sets the current status step state.
     *
     * @param success
     *         {@code true} if success, {@code false} otherwise.
     */
    void setCurrentStatusStepStatus(boolean success);

    /**
     * Shows the status section message.
     *
     * @param error
     *         {@code true} if the message displayed is an error, {@code false} otherwise.
     */
    void showStatusSectionMessage(String message, boolean error);

    /**
     * Hides the status section message.
     */
    void hideStatusSectionMessage();

    /**
     * Hides the status section.
     */
    void hideStatusSection();

    /**
     * Show the new contribution section.
     *
     * @param vcsHostName
     *         the VCS host name.
     */
    void showNewContributionSection(String vcsHostName);

    /**
     * Hide the new contribution section.
     */
    void hideNewContributionSection();

    /**
     * Defines if the contribution is in progress.
     *
     * @param progress
     *         {@code true} if the contribution is in progress, {@code false} otherwise.
     */
    void setContributionProgressState(boolean progress);

    String getCurrentStatusStepName();

    /**
     * Action delegate interface for the contribution configuration dialog.
     */
    interface ActionDelegate extends BaseActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Contribute button. */
        void onContribute();

        /** Performs any action appropriate in response to the user having pressed the open pull request on vcs host button. */
        void onOpenPullRequestOnVcsHost();

        /** Performs any action appropriate in response to the user having pressed the start new contribution button. */
        void onNewContribution();

        /** Performs any action appropriate in response to the user having pressed the refresh contribution branch names list button. */
        void onRefreshContributionBranchNameList();

        /** Performs any action appropriate in response to the user having selected the create new branch item. */
        void onCreateNewBranch();

        /** Performs any action when view state is modified. */
        void updateControls();
    }
}
