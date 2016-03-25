/*******************************************************************************
 * Copyright (c) [2012] - [2016] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.core;

import com.codenvy.client.Request;
import com.codenvy.client.WorkspaceClient;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.core.model.DefaultWorkspace;
import com.codenvy.client.model.Workspace;
import com.google.common.reflect.TypeToken;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import java.lang.reflect.Type;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * The Codenvy workspace API client.
 *
 * @author Kevin Pollet
 * @author Stéphane Daviet
 */
public class DefaultWorkspaceClient extends AbstractClient implements WorkspaceClient {

    /* Composite key delimiter.
     * See {@link WorkspaceManager#getWorkspace(String key)}
     */
    private static final String KEY_DELIMITER = ":";

    /**
     * Constructs an instance of {@link DefaultWorkspaceClient}.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param authenticationManager
     *         the {@link AuthenticationManager}.
     * @throws NullPointerException
     *         if url or authenticationManager parameter is {@code null}.
     */
    DefaultWorkspaceClient(String url, AuthenticationManager authenticationManager) {
        super(url, "workspace", authenticationManager);
    }

    /**
     * Retrieves all Codenvy workspaces of the user identified by the authentication token.
     *
     * @return all Codenvy workspaces never {@code null}.
     */
    @Override
    public Request<List<Workspace>> all() {
        final Invocation request = getWebTarget().path("")
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();
        Type collectionType = new TypeToken<List<DefaultWorkspace>>() {
        }.getType();

        return new SimpleRequest<>(request, new GenericType<List<Workspace>>(collectionType) {
        }, getAuthenticationManager());
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
        checkNotNull(name);

        final Invocation request = getWebTarget().path(KEY_DELIMITER + name)
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();

        return new SimpleRequest<Workspace>(request, DefaultWorkspace.class, getAuthenticationManager());
    }

    /**
     * Creates the given workspace.
     *
     * @param workspaceReference
     *         the workspace to create.
     * @return the created workspace.
     * @throws NullPointerException
     *         if {@link com.codenvy.client.model.Workspace} parameter is {@code null}.
     */
    @Override
    public Request<Workspace> create(Workspace workspaceReference) {
        checkNotNull(workspaceReference);

        final Invocation request = getWebTarget().request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(json(workspaceReference));

        return new SimpleRequest<Workspace>(request, DefaultWorkspace.class, getAuthenticationManager());

    }
}
