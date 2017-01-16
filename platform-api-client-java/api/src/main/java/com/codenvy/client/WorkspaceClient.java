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
package com.codenvy.client;


import com.codenvy.client.model.Workspace;

import java.util.List;

/**
 * The Codenvy workspace API client.
 *
 * @author Florent Benoit
 */
public interface WorkspaceClient {
    /**
     * Retrieves all Codenvy workspaces of the user identified by the authentication token.
     *
     * @return all Codenvy workspaces never {@code null}.
     */
    Request<List<Workspace>> all();

    /**
     * Retrieves a Codenvy workspace by it's name.
     *
     * @param name
     *         the workspace name.
     * @return the Codenvy workspace or {@code null} if none.
     * @throws NullPointerException
     *         if name parameter is {@code null}.
     */
    Request<Workspace> withName(String name);

    /**
     * Creates the given workspace.
     *
     * @param workspace
     *         the workspace to create.
     * @return the created workspace.
     * @throws NullPointerException
     *         if {@link com.codenvy.client.model.Workspace} parameter is {@code null}.
     */
    Request<Workspace> create(Workspace workspace);
}
