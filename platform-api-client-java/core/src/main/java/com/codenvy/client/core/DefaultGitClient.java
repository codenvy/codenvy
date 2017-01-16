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
package com.codenvy.client.core;

import com.codenvy.client.GitClient;
import com.codenvy.client.Request;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.core.model.git.DefaultInitRequest;
import com.codenvy.client.core.model.git.DefaultLog;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.git.InitRequest;
import com.codenvy.client.model.git.Log;
import com.codenvy.client.model.git.LogRequest;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * The Codenvy git API client.
 *
 * @author Florent Benoit
 */
public class DefaultGitClient extends AbstractClient implements GitClient {

    /**
     * Constructs an instance of {@link com.codenvy.client.GitClient}.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param authenticationManager
     *         the {@link com.codenvy.client.core.auth.AuthenticationManager}.
     * @throws NullPointerException
     *         if url or authenticationManager parameter is {@code null}.
     */
    DefaultGitClient(String url, AuthenticationManager authenticationManager) {
        super(url, "git", authenticationManager);
    }

    /**
     * Retrieves a Git read-only URL for the given project.
     *
     * @param projectReference
     *         the project reference.
     * @return a GIT URL that allow to clone the given project.
     */
    public Request<String> readOnlyUrl(ProjectReference projectReference) {

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("read-only-url")
                                                 .queryParam("projectPath", projectReference.name())
                                                 .request()
                                                 .accept(MediaType.WILDCARD)
                                                 .buildGet();
        return new SimpleRequest<>(request, String.class, getAuthenticationManager());
    }

    /**
     * Initialize a Git repository for the given project.
     *
     * @param projectReference
     *         the project reference.
     * @return nothing.
     */
    @Override
    public Request<Void> init(ProjectReference projectReference) {
        // Create request
        InitRequest initRequest = new DefaultInitRequest(projectReference.name(), false, true);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("init")
                                                 .queryParam("projectPath", projectReference.name())
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(json(initRequest));
        return new SimpleRequest<>(request, Void.class, getAuthenticationManager());
    }

    /**
     * Get log data for the given
     *
     * @param projectReference
     *         the project reference.
     * @param logRequest
     *         the log request.
     * @return a log containing all revisions
     */
    @Override
    public Request<Log> log(ProjectReference projectReference, LogRequest logRequest) {
        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("log")
                                                 .queryParam("projectPath", projectReference.name())
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(json(logRequest));
        return new SimpleRequest<Log>(request, DefaultLog.class, getAuthenticationManager());
    }
}
