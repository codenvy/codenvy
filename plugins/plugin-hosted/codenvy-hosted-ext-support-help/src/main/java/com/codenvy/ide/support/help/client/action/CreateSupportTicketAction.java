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
import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;


/**
 * Show the UserVoice widget on the screen for create the support ticket.
 *
 * @author Oleksii Orel
 */
public class CreateSupportTicketAction extends Action {


    @Inject
    public CreateSupportTicketAction(HelpLocalizationConstant locale,
                                     HelpResources resources) {
        super(locale.actionCreateSupportTicketDescription(), locale.actionCreateSupportTicketTitle(), null, resources.createTicket());
    }


    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        showWidget();
    }

    private static native void showWidget() /*-{
        $wnd.UserVoice.showPopupWidget({mode: 'support'});
    }-*/;
}
