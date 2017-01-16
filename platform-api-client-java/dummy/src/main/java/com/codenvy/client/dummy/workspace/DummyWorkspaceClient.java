/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.dummy.workspace;

import com.codenvy.client.Request;
import com.codenvy.client.WorkspaceClient;
import com.codenvy.client.dummy.DummyRequest;
import com.codenvy.client.model.Workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Florent Benoit
 */
public class DummyWorkspaceClient implements WorkspaceClient {

    private Map<String, Workspace> workspaces;

    public DummyWorkspaceClient() {
        this.workspaces = new HashMap<>();
    }


    /**
     * Retrieves all Codenvy workspaces of the user identified by the authentication token.
     *
     * @return all Codenvy workspaces never {@code null}.
     */
    @Override
    public Request<List<Workspace>> all() {

        List<Workspace> workspacesToReturn = new ArrayList<>();
        for (Workspace dummyWorkspace : workspaces.values()) {
            workspacesToReturn.add(dummyWorkspace);
        }

        return new DummyRequest<>(workspacesToReturn);
    }

    /**
     * Retrieves a Codenvy workspace by it's name.
     *
     * @param name
     *         the workspace name.
     * @return the Codenvy workspace or {@code null} if none.
     * @throws NullPointerException
     *         if name parameter is {@code null}.
     */
    @Override
    public Request<Workspace> withName(String name) {
        for (Map.Entry<String, Workspace> entry : workspaces.entrySet()) {
            if (name.equals(entry.getKey())) {
                return new DummyRequest<Workspace>(entry.getValue());
            }
        }
        return new DummyRequest<>(null);
    }

    /**
     * Creates the given workspace.
     *
     * @param workspace
     *         the workspace to create.
     * @return the created workspace.
     * @throws NullPointerException
     *         if {@link com.codenvy.client.model.Workspace} parameter is {@code null}.
     */
    @Override
    public Request<Workspace> create(Workspace workspace) {
        registerWorkspace(workspace);
        return new DummyRequest<Workspace>(workspace);
    }


    public void registerWorkspace(Workspace workspace) {
        workspaces.put(workspace.name(), workspace);
    }
}
