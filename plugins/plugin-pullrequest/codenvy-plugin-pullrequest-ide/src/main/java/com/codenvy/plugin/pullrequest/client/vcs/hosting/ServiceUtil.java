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
package com.codenvy.plugin.pullrequest.client.vcs.hosting;

import com.codenvy.plugin.pullrequest.shared.dto.HostUser;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;

/**
 * Utils for {@link VcsHostingService} implementations.
 *
 * @author Yevhenii Voevodin
 */
public final class ServiceUtil {

    /**
     * Performs {@link JsOAuthWindow} authentication and tries to get current user.
     *
     * @param service
     *         hosting service, used to authorized user
     * @param authUrl
     *         url to perform authentication
     * @return the promise which resolves authorized user or rejects with an error
     */
    public static Promise<HostUser> performWindowAuth(final VcsHostingService service, final String authUrl) {
        final Executor.ExecutorBody<HostUser> exBody = new Executor.ExecutorBody<HostUser>() {
            @Override
            public void apply(final ResolveFunction<HostUser> resolve, final RejectFunction reject) {
                new JsOAuthWindow(authUrl, "error.url", 500, 980, new OAuthCallback() {
                    @Override
                    public void onAuthenticated(final OAuthStatus authStatus) {
                        // maybe it's possible to avoid this request if authStatus contains the vcs host user.
                        service.getUserInfo().then(new Operation<HostUser>() {
                            @Override
                            public void apply(HostUser user) throws OperationException {
                                resolve.apply(user);
                            }
                        }).catchError(new Operation<PromiseError>() {
                            @Override
                            public void apply(PromiseError error) throws OperationException {
                                reject.apply(error);
                            }
                        });
                    }
                }).loginWithOAuth();
            }
        };
        return Promises.create(Executor.create(exBody));
    }

    private ServiceUtil() {}
}
