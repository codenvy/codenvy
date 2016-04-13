/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.ide.factory.client;

import com.codenvy.ide.factory.client.accept.AcceptFactoryHandler;
import com.codenvy.ide.factory.client.action.CreateFactoryAction;
import com.codenvy.ide.factory.client.json.ImportFromConfigAction;
import com.codenvy.ide.factory.client.welcome.GreetingPartPresenter;
import com.codenvy.ide.factory.client.welcome.OpenWelcomePageAction;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;

import static com.google.gwt.core.client.ScriptInjector.TOP_WINDOW;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_PROJECT;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_WORKSPACE;

/**
 * @author Vladyslav Zhukovskii
 */
@Singleton
@Extension(title = "Factory", version = "3.0.0")
public class FactoryExtension {

    @Inject
    public FactoryExtension(AcceptFactoryHandler acceptFactoryHandler,
                            ActionManager actionManager,
                            FactoryResources resources,
                            CreateFactoryAction configureFactoryAction,
                            ImportFromConfigAction importFromConfigAction,
                            GreetingPartPresenter greetingPartPresenter,
                            OpenWelcomePageAction openWelcomePageAction) {
        acceptFactoryHandler.process();

        /*
         * Inject resources and js
         */
        ScriptInjector.fromUrl("https://apis.google.com/js/client:plusone.js?parsetags=explicit")
                      .setWindow(TOP_WINDOW)
                      .inject();

        ScriptInjector.fromUrl("https://connect.facebook.net/en_US/sdk.js")
                      .setWindow(TOP_WINDOW)
                      .setCallback(new Callback<Void, Exception>() {
                          @Override
                          public void onSuccess(Void result) {
                              init();
                          }

                          @Override
                          public void onFailure(Exception reason) {
                          }

                          private native void init() /*-{
                              $wnd.FB.init({
                                  appId: "318167898391385",
                                  xfbml: true,
                                  version: "v2.1"
                              });
                          }-*/;
                      }).inject();

        resources.factoryCSS().ensureInjected();

        DefaultActionGroup projectGroup = (DefaultActionGroup)actionManager.getAction(GROUP_PROJECT);
        DefaultActionGroup workspaceGroup = (DefaultActionGroup)actionManager.getAction(GROUP_WORKSPACE);

        actionManager.registerAction("openWelcomePage", openWelcomePageAction);
        actionManager.registerAction("importProjectFromCodenvyConfigAction", importFromConfigAction);
        actionManager.registerAction("configureFactoryAction", configureFactoryAction);

        projectGroup.add(importFromConfigAction);
        workspaceGroup.add(configureFactoryAction);
    }
}
