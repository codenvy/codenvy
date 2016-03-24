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
package com.codenvy.plugin.pullrequest.client.steps;

import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.pullrequest.client.dto.Repository;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import javax.validation.constraints.NotNull;

public class WaitForkOnRemoteStep implements Step {
    private static final int POLL_FREQUENCY_MS = 1000;

    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final Step                      nextStep;
    private       Timer                     timer;

    @AssistedInject
    public WaitForkOnRemoteStep(@NotNull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                @NotNull final @Assisted Step nextStep) {
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.nextStep = nextStep;
    }

    @Override
    public void execute(@NotNull final WorkflowExecutor executor, final Context context) {
        if (timer == null) {
            timer = new Timer() {
                @Override
                public void run() {
                    checkRepository(context, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(final Throwable caught) {
                            timer.schedule(POLL_FREQUENCY_MS);
                        }

                        @Override
                        public void onSuccess(final Void result) {
                            executor.done(WaitForkOnRemoteStep.this, context);
                        }
                    });
                }
            };
        }

        timer.schedule(POLL_FREQUENCY_MS);
    }

    private void checkRepository(final Context context, final AsyncCallback<Void> callback) {
        vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }

            @Override
            public void onSuccess(final VcsHostingService vcsHostingService) {
                vcsHostingService
                        .getRepository(context.getHostUserLogin(), context.getForkedRepositoryName(), new AsyncCallback<Repository>() {
                            @Override
                            public void onFailure(final Throwable exception) {
                                callback.onFailure(exception);
                            }

                            @Override
                            public void onSuccess(final Repository repository) {
                                callback.onSuccess(null);
                            }
                        });
            }
        });
    }
}
