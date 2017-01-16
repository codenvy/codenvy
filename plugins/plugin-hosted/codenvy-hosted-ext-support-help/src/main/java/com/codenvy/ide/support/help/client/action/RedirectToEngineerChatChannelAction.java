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
package com.codenvy.ide.support.help.client.action;

import com.codenvy.ide.support.help.client.HelpLocalizationConstant;
import com.codenvy.ide.support.help.client.HelpResources;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;

/**
 * Open a new window with the IRC channel URL
 *
 * @author Oleksii Orel
 */
public class RedirectToEngineerChatChannelAction extends Action {
    private final HelpLocalizationConstant locale;

    @Inject
    public RedirectToEngineerChatChannelAction(HelpLocalizationConstant locale,
                                               HelpResources resources) {
        super(locale.actionRedirectToEngineerChatChannelTitle(), locale.actionRedirectToEngineerChatChannelDescription(), null,
              resources.ircChannel());
        this.locale = locale;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        Window.open(locale.actionRedirectToEngineerChatChannelUrl(), "_blank", "");
    }
}
