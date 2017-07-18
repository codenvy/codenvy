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
