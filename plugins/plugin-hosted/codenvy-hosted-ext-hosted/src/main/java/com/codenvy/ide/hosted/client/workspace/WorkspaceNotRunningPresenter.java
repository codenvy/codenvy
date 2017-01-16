/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.ide.hosted.client.workspace;

import com.google.gwt.user.client.Window;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This window is shown when workspace has been stopped not by user
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class WorkspaceNotRunningPresenter implements WorkspaceNotRunningView.ActionDelegate {

    private final WorkspaceNotRunningView view;

    @Inject
    public WorkspaceNotRunningPresenter(WorkspaceNotRunningView view) {
        this.view = view;
        this.view.setDelegate(this);
    }

    /**
     * Show dialog
     */
    public void show() {
        view.show();
    }

    @Override
    public native void onOpenDashboardButtonClicked() /*-{
        // Verify if IDE is loaded in frame
        if ($wnd.parent == $wnd) {
            // IDE is not in frame
            // Just replace the URL
            $wnd.location.replace("/dashboard/#/workspaces");
        } else {
            // IDE is in frame
            // Send a message to the parent frame to open workspaces
            $wnd.parent.postMessage("show-workspaces", "*");
        }
    }-*/;

    @Override
    public void onRestartButtonClicked() {
        Window.Location.reload();
    }

}
