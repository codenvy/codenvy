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
import org.eclipse.che.ide.util.browser.BrowserUtils;

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
        BrowserUtils.openInNewTab(locale.actionRedirectToEngineerChatChannelUrl());
    }
}
