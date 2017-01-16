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
