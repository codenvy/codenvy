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

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;

/**
 * @author Sergii Leschenko
 */
public class OpenWelcomePageAction extends Action {
    private final GreetingPartPresenter greetingPart;

    @Inject
    public OpenWelcomePageAction(GreetingPartPresenter greetingPart) {
        this.greetingPart = greetingPart;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getParameters() == null) {
            Log.error(getClass(), "Can't show welcome page without parameters");
            return;
        }

        greetingPart.showGreeting(e.getParameters());
    }
}
