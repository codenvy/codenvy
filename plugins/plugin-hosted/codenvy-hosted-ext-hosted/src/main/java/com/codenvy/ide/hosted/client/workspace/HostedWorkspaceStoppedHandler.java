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
package com.codenvy.ide.hosted.client.workspace;

import com.google.gwt.core.client.Callback;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.workspace.start.StartWorkspacePresenter;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.List;

import static com.codenvy.activity.shared.Constants.ACTIVITY_CHECKER;

/**
 * Shows dialog after workspace stopped event.
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class HostedWorkspaceStoppedHandler implements WorkspaceStoppedEvent.Handler {

    private final static int SKIP_COUNT = 0;
    private final static int MAX_COUNT  = 10;

    private final Provider<WorkspaceNotRunningPresenter> workspaceNotRunningPresenterProvider;
    private final Provider<StartWorkspacePresenter>      startWorkspacePresenterProvider;
    private final WorkspaceServiceClient                 workspaceServiceClient;
    private final Callback<Component, Exception>         callback;

    @Inject
    public HostedWorkspaceStoppedHandler(Provider<WorkspaceNotRunningPresenter> workspaceNotRunningPresenterProvider,
                                         Provider<StartWorkspacePresenter> startWorkspacePresenterProvider,
                                         WorkspaceServiceClient workspaceServiceClient,
                                         EventBus eventBus) {
        this.workspaceNotRunningPresenterProvider = workspaceNotRunningPresenterProvider;
        this.startWorkspacePresenterProvider = startWorkspacePresenterProvider;
        this.workspaceServiceClient = workspaceServiceClient;

        this.callback = new Callback<Component, Exception>() {
            @Override
            public void onFailure(Exception reason) {
            }

            @Override
            public void onSuccess(Component result) {
            }
        };

        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
    }

    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        workspaceServiceClient.getWorkspace(event.getWorkspace().getId()).then(new Operation<WorkspaceDto>() {
            @Override
            public void apply(WorkspaceDto workspace) throws OperationException {
                if (ACTIVITY_CHECKER.equals(workspace.getAttributes().get(Constants.WORKSPACE_STOPPED_BY))) {
                    workspaceNotRunningPresenterProvider.get().show();
                } else {
                    workspaceServiceClient.getWorkspaces(SKIP_COUNT, MAX_COUNT)
                                          .then(new Operation<List<WorkspaceDto>>() {
                                              @Override
                                              public void apply(List<WorkspaceDto> workspaces) throws OperationException {
                                                  startWorkspacePresenterProvider.get().show(workspaces, callback);
                                              }
                                          });
                }
            }
        });
    }
}
