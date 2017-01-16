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
package com.codenvy.ide.factory.client.json;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * The view of {@link ImportFromConfigPresenter}.
 *
 * @author Sergii Leschenko
 */
public interface ImportFromConfigView extends IsWidget {

    public interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Cancel button. */
        void onCancelClicked();

        /** Performs any actions appropriate in response to the user having pressed the Import button. */
        void onImportClicked();

        /** Performs any actions appropriate in response to error reading file */
        void onErrorReadingFile(String errorMessage);
    }

    /** Show dialog. */
    void showDialog();

    /** Close dialog */
    void closeDialog();

    /** Sets the delegate to receive events from this view. */
    void setDelegate(ActionDelegate delegate);

    /** Enables or disables import button */
    void setEnabledImportButton(boolean enabled);

    /** Get content of selected file */
    String getFileContent();
}
