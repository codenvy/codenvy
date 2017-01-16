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

import com.codenvy.client.Request;
import com.codenvy.client.RunnerClient;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.core.model.DefaultRunnerStatus;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.RunnerStatus;
import com.codenvy.client.model.runner.RunOptions;
import com.google.common.reflect.TypeToken;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import java.lang.reflect.Type;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * The Codenvy runner API client.
 *
 * @author Kevin Pollet
 */
public class DefaultRunnerClient extends AbstractClient implements RunnerClient {
    /**
     * Constructs an instance of {@link RunnerClient}.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param authenticationManager
     *         the {@link AuthenticationManager}.
     * @throws NullPointerException
     *         if url or authenticationManager parameter is {@code null}.
     */
    DefaultRunnerClient(String url, AuthenticationManager authenticationManager) {
        super(url, "runner", authenticationManager);
    }

    /**
     * Runs the given project with a codenvy runner.
     *
     * @param projectReference
     *         the project to run.
     * @return the {@link RunnerStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<RunnerStatus> run(ProjectReference projectReference) {
        return run(projectReference, null);
    }

    /**
     * Runs the given project with a codenvy runner.
     *
     * @param projectReference
     *         the project to run.
     * @param runOptions
     *         the options for the runner
     * @return the {@link com.codenvy.client.model.RunnerStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<RunnerStatus> run(ProjectReference projectReference, RunOptions runOptions) {
        checkNotNull(projectReference);

        String projectPath = projectReference.name();
        if (!projectPath.startsWith("/")) {
            projectPath = "/".concat(projectPath);
        }

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("run")
                                                 .queryParam("project", projectPath)
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(json(runOptions));

        return new SimpleRequest<RunnerStatus>(request, DefaultRunnerStatus.class, getAuthenticationManager());
    }

    /**
     * Stops the project runner with the given process id.
     *
     * @param projectReference
     *         the project.
     * @param processId
     *         the runner process id.
     * @return the {@link RunnerStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<RunnerStatus> stop(ProjectReference projectReference, long processId) {
        checkNotNull(projectReference);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("stop")
                                                 .path(String.valueOf(processId))
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(null);

        return new SimpleRequest<RunnerStatus>(request, DefaultRunnerStatus.class, getAuthenticationManager());
    }


    /**
     * Gets the project processes for the given project
     *
     * @param projectReference
     *         the project.
     * @return the different statuses.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    public Request<List<RunnerStatus>> processes(ProjectReference projectReference) {
        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("processes")
                                                 .queryParam("project", projectReference.name())
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();

        Type collectionType = new TypeToken<List<DefaultRunnerStatus>>() {
        }.getType();

        return new SimpleRequest<>(request, new GenericType<List<RunnerStatus>>(collectionType) {
        }, getAuthenticationManager());


    }

    /**
     * Gets the project runner status with the given process id.
     *
     * @param projectReference
     *         the project.
     * @param processId
     *         the runner process id.
     * @return the {@link RunnerStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<RunnerStatus> status(ProjectReference projectReference, long processId) {
        checkNotNull(projectReference);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("status")
                                                 .path(String.valueOf(processId))
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();

        return new SimpleRequest<RunnerStatus>(request, DefaultRunnerStatus.class, getAuthenticationManager());
    }

    /**
     * Gets the project runner logs with the given process id.
     *
     * @param projectReference
     *         the project.
     * @param processId
     *         the runner process id.
     * @return the runner logs.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<String> logs(ProjectReference projectReference, long processId) {
        checkNotNull(projectReference);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("logs")
                                                 .path(String.valueOf(processId))
                                                 .request()
                                                 .accept(TEXT_PLAIN)
                                                 .buildGet();

        return new SimpleRequest<>(request, String.class, getAuthenticationManager());
    }
}
