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
package com.codenvy.ide.factory.client.welcome;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * @author Vitaliy Guliy
 */
public interface GreetingPartView extends View<GreetingPartView.ActionDelegate> {

    public interface ActionDelegate extends BaseActionDelegate {
    }

    /**
     * Set title of greeting part.
     *
     * @param title
     *         title that need to be set
     */
    void setTitle(String title);

    /**
     * Sets new URL of greeting page.
     *
     * @param url
     */
    void showGreeting(String url);

    void setVisible(boolean visible);

}
