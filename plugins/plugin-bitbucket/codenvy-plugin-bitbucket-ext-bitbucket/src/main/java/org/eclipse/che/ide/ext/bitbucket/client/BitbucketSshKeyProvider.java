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
package org.eclipse.che.ide.ext.bitbucket.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import org.eclipse.che.ide.ext.git.ssh.client.SshKeyUploader;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import static org.eclipse.che.security.oauth.OAuthStatus.LOGGED_IN;

/**
 * Provides SSH keys for bitbucket.org and deploys it.
 *
 * @author Kevin Pollet
 */
@Singleton
public class BitbucketSshKeyProvider implements SshKeyUploader, OAuthCallback {
    private final BitbucketClientService        bitbucketService;
    private final String                        baseUrl;
    private final BitbucketLocalizationConstant constant;
    private final NotificationManager           notificationManager;
    private final DialogFactory                 dialogFactory;
    private final AppContext                    appContext;
    private       AsyncCallback<Void>           callback;
    private       String                        userId;

    @Inject
    public BitbucketSshKeyProvider(@NotNull final BitbucketClientService bitbucketService,
                                   @NotNull @RestContext final String baseUrl,
                                   @NotNull final BitbucketLocalizationConstant constant,
                                   @NotNull final NotificationManager notificationManager,
                                   @NotNull final DialogFactory dialogFactory,
                                   AppContext appContext) {

        this.bitbucketService = bitbucketService;
        this.baseUrl = baseUrl;
        this.constant = constant;
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
        this.appContext = appContext;
    }

    @Override
    public void uploadKey(String userId, final AsyncCallback<Void> callback) {
        this.callback = callback;
        this.userId = userId;

        bitbucketService.generateAndUploadSSHKey(new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(final Void notUsed) {
                callback.onSuccess(notUsed);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                if (exception instanceof UnauthorizedException) {
                    oAuthLoginStart();
                    return;
                }

                callback.onFailure(exception);
            }
        });
    }

    private void oAuthLoginStart() {
        dialogFactory.createConfirmDialog(constant.bitbucketSshKeyTitle(), constant.bitbucketSshKeyLabel(), new ConfirmCallback() {
            @Override
            public void accepted() {
                showPopUp();
            }
        }, null).show();
    }

    private void showPopUp() {
        final String authUrl = baseUrl + "/oauth/1.0/authenticate?oauth_provider=bitbucket&userId=" + userId + "&redirect_after_login=" +
                               Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/ws/" +
                               appContext.getWorkspace().getConfig().getName();

        new JsOAuthWindow(authUrl, "error.url", 500, 980, this).loginWithOAuth();
    }

    @Override
    public void onAuthenticated(final OAuthStatus authStatus) {
        if (LOGGED_IN.equals(authStatus)) {
            uploadKey(userId, callback);
        } else {
            notificationManager.notify(constant.bitbucketSshKeyUpdateFailed());
        }
    }
}
