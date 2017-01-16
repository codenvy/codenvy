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
package com.codenvy.ide.hosted.client.notifier;


import org.eclipse.che.ide.api.mvp.View;

/**
 * View that informs user about bad connection.
 *
 * @author Anton Korneta
 */
public interface BadConnectionNotifierView extends View<BadConnectionNotifierView.ActionDelegate> {

    interface ActionDelegate {

        /** delegates {@link BadConnectionNotifierView#close()} */
        void onOkClicked();
    }

    /** opens information popup */
    void showDialog(String title, String message);

    /** close information popup */
    void close();
}
