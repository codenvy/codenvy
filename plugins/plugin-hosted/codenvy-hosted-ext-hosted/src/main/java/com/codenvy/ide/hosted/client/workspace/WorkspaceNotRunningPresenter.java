/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
