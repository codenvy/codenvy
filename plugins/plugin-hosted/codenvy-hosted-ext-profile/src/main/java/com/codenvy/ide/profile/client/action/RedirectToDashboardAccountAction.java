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
package com.codenvy.ide.profile.client.action;

import com.codenvy.ide.profile.client.ProfileLocalizationConstant;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;

/**
 * Open a new dashboard window with account information
 *
 * @author Oleksii Orel
 */
public class RedirectToDashboardAccountAction extends Action {
    private final ProfileLocalizationConstant locale;

    @Inject
    public RedirectToDashboardAccountAction(ProfileLocalizationConstant locale) {
        super(locale.redirectToDashboardAccountTitle(), locale.redirectToDashboardAccountDescription(), null, null);
        this.locale = locale;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        Window.open(locale.redirectToDashboardAccountUrl(), "_blank", "");
    }
}
