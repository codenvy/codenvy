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
package com.codenvy.ide.factory.client.inject;

import com.codenvy.ide.factory.client.json.ImportFromConfigView;
import com.codenvy.ide.factory.client.json.ImportFromConfigViewImpl;
import com.codenvy.ide.factory.client.welcome.GreetingPartView;
import com.codenvy.ide.factory.client.welcome.GreetingPartViewImpl;
import com.codenvy.ide.factory.client.welcome.preferences.ShowWelcomePreferencePagePresenter;
import com.codenvy.ide.factory.client.welcome.preferences.ShowWelcomePreferencePageView;
import com.codenvy.ide.factory.client.welcome.preferences.ShowWelcomePreferencePageViewImpl;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;

import javax.inject.Singleton;

/**
 * @author Vladyslav Zhukovskii
 */
@ExtensionGinModule
public class FactoryGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(GreetingPartView.class).to(GreetingPartViewImpl.class).in(Singleton.class);
        bind(ImportFromConfigView.class).to(ImportFromConfigViewImpl.class).in(Singleton.class);
        bind(ShowWelcomePreferencePageView.class).to(ShowWelcomePreferencePageViewImpl.class).in(Singleton.class);

        final GinMultibinder<PreferencePagePresenter> prefBinder = GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
        prefBinder.addBinding().to(ShowWelcomePreferencePagePresenter.class);
    }
}
