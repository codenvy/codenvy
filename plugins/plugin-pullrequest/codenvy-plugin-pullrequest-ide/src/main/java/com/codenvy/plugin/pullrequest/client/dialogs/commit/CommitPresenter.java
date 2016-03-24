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

import com.codenvy.plugin.pullrequest.client.utils.NotificationHelper;
import com.codenvy.plugin.pullrequest.client.vcs.VcsServiceProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;

import javax.validation.constraints.NotNull;

import static com.codenvy.plugin.pullrequest.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.CANCEL;
import static com.codenvy.plugin.pullrequest.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.CONTINUE;
import static com.codenvy.plugin.pullrequest.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.OK;
import static org.eclipse.che.ide.ext.git.client.GitRepositoryInitializer.isGitRepository;

/**
 * This presenter provides base functionality to commit project changes or not before cloning or generating a factory url.
 *
 * @author Kevin Pollet
 */
public class CommitPresenter implements CommitView.ActionDelegate {

    private final CommitView          view;
    private final AppContext          appContext;
    private final VcsServiceProvider  vcsServiceProvider;
    private final NotificationHelper  notificationHelper;
    private       CommitActionHandler handler;

    @Inject
    public CommitPresenter(@NotNull final CommitView view,
                           @NotNull final AppContext appContext,
                           @NotNull final VcsServiceProvider vcsServiceProvider,
                           @NotNull final NotificationHelper notificationHelper) {
        this.view = view;
        this.appContext = appContext;
        this.vcsServiceProvider = vcsServiceProvider;
        this.notificationHelper = notificationHelper;

        this.view.setDelegate(this);
    }

    /**
     * Opens the {@link CommitView}.
     *
     * @param commitDescription
     *         the default commit description.
     */
    public void showView(@NotNull String commitDescription) {
        view.show(commitDescription);
    }

    /**
     * Sets the {@link CommitPresenter.CommitActionHandler} called after the ok or
     * continue action is
     * executed.
     *
     * @param handler
     *         the handler to set.
     */
    public void setCommitActionHandler(final CommitActionHandler handler) {
        this.handler = handler;
    }

    /**
     * Returns if the current project has uncommitted changes.
     */
    public void hasUncommittedChanges(final AsyncCallback<Boolean> callback) {
        final CurrentProject project = appContext.getCurrentProject();
        if (project == null) {
            callback.onFailure(new IllegalStateException("No project opened"));

        } else if (!isGitRepository(project.getRootProject())) {
            callback.onFailure(new IllegalStateException("Opened project is not has no Git repository"));

        } else {
            vcsServiceProvider.getVcsService(project.getRootProject()).hasUncommittedChanges(project.getRootProject(), callback);
        }
    }

    @Override
    public void onOk() {
        final CurrentProject project = appContext.getCurrentProject();
        if (project != null) {
            vcsServiceProvider.getVcsService(project.getRootProject()).commit(project.getRootProject(), view.isIncludeUntracked(),
                                                      view.getCommitDescription(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(final Throwable exception) {
                            notificationHelper.showError(CommitPresenter.class, exception);
                        }

                        @Override
                        public void onSuccess(final Void result) {
                            view.close();

                            if (handler != null) {
                                handler.onCommitAction(OK);
                            }
                        }
                    });
        }
    }

    @Override
    public void onContinue() {
        view.close();

        if (handler != null) {
            handler.onCommitAction(CONTINUE);
        }
    }

    @Override
    public void onCancel() {
        view.close();

        if (handler != null) {
            handler.onCommitAction(CANCEL);
        }
    }

    @Override
    public void onCommitDescriptionChanged() {
        view.setOkButtonEnabled(!view.getCommitDescription().isEmpty());
    }

    public interface CommitActionHandler {
        /**
         * Called when a commit actions is done on the commit view.
         *
         * @param action
         *         the action.
         */
        void onCommitAction(CommitAction action);

        enum CommitAction {
            OK,
            CONTINUE,
            CANCEL
        }
    }
}
