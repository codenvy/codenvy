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
package com.codenvy.ide.hosted.client.informers;

import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.HttpSessionDestroyedEvent;
import org.eclipse.che.ide.api.event.HttpSessionDestroyedHandler;
import org.eclipse.che.ide.api.user.UserServiceClient;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import com.codenvy.ide.hosted.client.HostedLocalizationConstant;
import com.codenvy.ide.hosted.client.login.PromptToLoginView;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Notify that http session was destroyed.
 *
 * @author Evgen Vidolob
 * @author Sergii Leschenko
 */
@Singleton
public class HttpSessionDestroyedInformer implements HttpSessionDestroyedHandler {
    private final EventBus                   eventBus;
    private final PromptToLoginView          view;
    private final DtoUnmarshallerFactory     dtoUnmarshallerFactory;
    private final UserServiceClient          userServiceClient;
    private final AppContext                 appContext;
    private final HostedLocalizationConstant localizationConstant;

    @Inject
    HttpSessionDestroyedInformer(EventBus eventBus,
                                 PromptToLoginView view,
                                 DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                 UserServiceClient userServiceClient,
                                 AppContext appContext,
                                 HostedLocalizationConstant localizationConstant) {
        this.eventBus = eventBus;
        this.view = view;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.userServiceClient = userServiceClient;
        this.appContext = appContext;
        this.localizationConstant = localizationConstant;

        view.setDelegate(new PromptToLoginView.ActionDelegate() {
            @Override
            public void onLogin() {
                String separator = Window.Location.getQueryString().contains("?") ? "&" : "?";
                Window.Location.replace(Window.Location.getPath() + Window.Location.getQueryString() + separator + "login");
            }

            @Override
            public void onCreateAccount() {
                Window.Location.replace("/site/create-account");
            }
        });
    }

    public void process() {
        eventBus.addHandler(HttpSessionDestroyedEvent.TYPE, this);
    }

    @Override
    public void onHttpSessionDestroyed(HttpSessionDestroyedEvent event) {
        tryRestoreSession();
    }

    /**
     * Tries to restore the session by asking an API method.
     */
    private void tryRestoreSession() {
        userServiceClient.getCurrentUser(new AsyncRequestCallback<UserDescriptor>(
                dtoUnmarshallerFactory.newUnmarshaller(UserDescriptor.class)) {
            @Override
            protected void onSuccess(UserDescriptor result) {
                if (!appContext.getCurrentUser().getProfile().getId().equals(result.getId())) {
                    showPromptToLogin();
                }
            }

            @Override
            protected void onFailure(Throwable exception) {
                showPromptToLogin();
            }
        });
    }

    /**
     * Displays dialog using title and message.
     */
    private void showPromptToLogin() {
        view.showDialog(localizationConstant.sessionExpiredDialogTitle(), localizationConstant.sessionExpiredDialogMessage());
    }
}
