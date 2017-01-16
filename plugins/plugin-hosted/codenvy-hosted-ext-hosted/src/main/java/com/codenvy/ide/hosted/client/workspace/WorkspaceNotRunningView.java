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

/**
 * @author Mihail Kuznyetsov
 */
public interface WorkspaceNotRunningView {


    interface ActionDelegate {
        /** Performs some actions when user clicks on open dashboard button. */
        void onOpenDashboardButtonClicked();

        /** Performs some actions when user clicks on restart button. */
        void onRestartButtonClicked();
    }

    void show();

    void setDelegate(ActionDelegate delegate);
}
