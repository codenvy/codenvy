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
package com.codenvy.ide.hosted.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.rest.RestContext;

import javax.inject.Inject;

import static com.google.gwt.core.client.ScriptInjector.TOP_WINDOW;

/**
 * Adds activity tracking script to IDE.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
@Extension(title = "Activity Tracking Extension", version = "1.0.0")
public class ActivityTrackingExtension {

    @Inject
    public ActivityTrackingExtension(final @RestContext String restContext,
                                     final AppContext appContext) {

        ScriptInjector.fromUrl("/_app/activity.js")
                      .setWindow(TOP_WINDOW)
                      .setCallback(new Callback<Void, Exception>() {
                          @Override
                          public void onSuccess(Void result) {
                              init(restContext, appContext.getWorkspaceId());
                          }

                          @Override
                          public void onFailure(Exception reason) {
                          }

                          private native void init(String restContext, String wsId) /*-{
                              $wnd.ActivityTracker.init(restContext, wsId);
                          }-*/;
                      })
                      .inject();

    }
}
