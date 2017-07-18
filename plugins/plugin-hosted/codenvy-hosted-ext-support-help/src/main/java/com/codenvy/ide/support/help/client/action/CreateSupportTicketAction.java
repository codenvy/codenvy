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
