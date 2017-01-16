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

import com.codenvy.client.BuilderClient;
import com.codenvy.client.Request;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.core.model.DefaultBuilderStatus;
import com.codenvy.client.model.BuilderStatus;
import com.codenvy.client.model.ProjectReference;
import com.google.common.reflect.TypeToken;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import java.lang.reflect.Type;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * The Codenvy builder API client.
 *
 * @author Kevin Pollet
 */
public class DefaultBuilderClient extends AbstractClient implements BuilderClient {
    /**
     * Constructs an instance of {@link DefaultBuilderClient}.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param authenticationManager
     *         the {@link AuthenticationManager}.
     * @throws NullPointerException
     *         if url or authenticationManager parameter is {@code null}.
     */
    DefaultBuilderClient(String url, AuthenticationManager authenticationManager) {
        super(url, "builder", authenticationManager);
    }

    /**
     * Builds the given {@link com.codenvy.client.core.model.DefaultProjectReference} on codenvy.
     *
     * @param projectReference
     *         the project to build.
     * @return the {@link com.codenvy.client.core.model.DefaultBuilderStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<BuilderStatus> build(ProjectReference projectReference) {
        checkNotNull(projectReference);

        String projectPath = projectReference.name();
        if (!projectPath.startsWith("/")) {
            projectPath = "/".concat(projectPath);
        }

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("build")
                                                 .queryParam("project", projectPath)
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(null);

        return new SimpleRequest<BuilderStatus>(request, DefaultBuilderStatus.class, getAuthenticationManager());
    }

    /**
     * Gets the status of the builder with the given task id.
     *
     * @param projectReference
     *         the project.
     * @param taskId
     *         the builder task id.
     * @return the {@link com.codenvy.client.core.model.DefaultBuilderStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<BuilderStatus> status(ProjectReference projectReference, long taskId) {
        checkNotNull(projectReference);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("status")
                                                 .path(String.valueOf(taskId))
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();

        return new SimpleRequest<BuilderStatus>(request, DefaultBuilderStatus.class, getAuthenticationManager());
    }

    /**
     * Gets the logs of the builder with the given task id.
     *
     * @param projectReference
     *         the project.
     * @param taskId
     *         the builder task id.
     * @return the builder logs.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<String> logs(ProjectReference projectReference, long taskId) {
        checkNotNull(projectReference);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("logs")
                                                 .path(String.valueOf(taskId))
                                                 .request()
                                                 .accept(TEXT_PLAIN)
                                                 .buildGet();

        return new SimpleRequest<>(request, String.class, getAuthenticationManager());
    }

    /**
     * Cancels the builder with the given task id.
     *
     * @param projectReference
     *         the project.
     * @param taskId
     *         the builder task id.
     * @return the {@link com.codenvy.client.core.model.DefaultBuilderStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<BuilderStatus> cancel(ProjectReference projectReference, long taskId) {
        checkNotNull(projectReference);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("cancel")
                                                 .path(String.valueOf(taskId))
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();

        return new SimpleRequest<BuilderStatus>(request, DefaultBuilderStatus.class, getAuthenticationManager());
    }

    /**
     * Gets the project builds for the given project
     *
     * @param projectReference
     *         the project.
     * @return the different statuses.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    public Request<List<BuilderStatus>> builds(ProjectReference projectReference) {
        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("builds")
                                                 .queryParam("project", projectReference.name())
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();

        Type collectionType = new TypeToken<List<DefaultBuilderStatus>>() {
        }.getType();
        return new SimpleRequest<>(request, new GenericType<List<BuilderStatus>>(collectionType) {
        }, getAuthenticationManager());


    }
}
