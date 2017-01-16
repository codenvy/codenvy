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
package com.codenvy.ide.factory.client.welcome.preferences;

import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Vitaliy Guliy
 */
@Singleton
public class ShowWelcomePreferencePagePresenter extends AbstractPreferencePagePresenter implements ShowWelcomePreferencePageView.ActionDelegate {

    public static final String SHOW_WELCOME_PREFERENCE_KEY = "plugin-hosted.welcome";

    private ShowWelcomePreferencePageView   view;
    private PreferencesManager              preferencesManager;

    @Inject
    public ShowWelcomePreferencePagePresenter(FactoryLocalizationConstant localizationConstant,
                                              ShowWelcomePreferencePageView view,
                                              PreferencesManager preferencesManager) {
        super(localizationConstant.welcomePreferencesTitle());
        this.view = view;
        this.preferencesManager = preferencesManager;
        view.setDelegate(this);
        view.welcomeField().setValue(true);
    }

    @Override
    public boolean isDirty() {
        String value = preferencesManager.getValue(SHOW_WELCOME_PREFERENCE_KEY);
        if (value == null) {
            return !view.welcomeField().getValue();
        }

        return !view.welcomeField().getValue().equals(Boolean.parseBoolean(value));
    }

    @Override
    public void storeChanges() {
        preferencesManager.setValue(SHOW_WELCOME_PREFERENCE_KEY, view.welcomeField().getValue().toString());
    }

    @Override
    public void revertChanges() {
        view.welcomeField().setValue(Boolean.parseBoolean(preferencesManager.getValue(SHOW_WELCOME_PREFERENCE_KEY)));
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
        String value = preferencesManager.getValue(SHOW_WELCOME_PREFERENCE_KEY);
        if (value == null) {
            view.welcomeField().setValue(true);
        } else {
            view.welcomeField().setValue(Boolean.parseBoolean(preferencesManager.getValue(SHOW_WELCOME_PREFERENCE_KEY)));
        }
    }

    @Override
    public void onDirtyChanged() {
        delegate.onDirtyChanged();
    }

}
