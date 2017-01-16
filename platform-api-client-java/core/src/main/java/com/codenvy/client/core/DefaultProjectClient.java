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

import com.codenvy.client.ProjectClient;
import com.codenvy.client.Request;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.core.model.DefaultProject;
import com.codenvy.client.core.model.DefaultProjectReference;
import com.codenvy.client.core.model.project.DefaultImportResponse;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.Visibility;
import com.codenvy.client.model.project.ImportResponse;
import com.google.common.reflect.TypeToken;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipInputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.client.Entity.text;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.fromStatusCode;

/**
 * The Codenvy project API client.
 *
 * @author Kevin Pollet
 * @author Stéphane Daviet
 */
public class DefaultProjectClient extends AbstractClient implements ProjectClient {
    /**
     * Constructs an instance of {@link DefaultProjectClient}.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param authenticationManager
     *         the {@link AuthenticationManager}.
     * @throws NullPointerException
     *         if url or authenticationManager parameter is {@code null}.
     */
    DefaultProjectClient(String url, AuthenticationManager authenticationManager) {
        super(url, "project", authenticationManager);
    }

    /**
     * Retrieves all workspace {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param workspaceId
     *         the workspace id.
     * @return the workspace {@link com.codenvy.client.model.ProjectReference} list never {@code null}.
     * @throws NullPointerException
     *         if workspaceId parameter is {@code null}.
     */
    @Override
    public Request<List<ProjectReference>> getWorkspaceProjects(String workspaceId) {
        checkNotNull(workspaceId);

        final Invocation request = getWebTarget().path(workspaceId)
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();

        Type collectionType = new TypeToken<List<DefaultProjectReference>>() {
        }.getType();
        return new SimpleRequest<>(request, new GenericType<List<ProjectReference>>(collectionType) {
        }, getAuthenticationManager());
    }

    /**
     * Retrieves project workspace {@link com.codenvy.client.model.Project}.
     *
     * @param workspaceId
     *         the workspace id.
     * @param resourcePath
     *         the resource path
     * @return the workspace {@link com.codenvy.client.model.Project}
     * @throws NullPointerException
     *         if workspaceId parameter is {@code null}.
     */
    @Override
    public Request<Project> getProject(String workspaceId, String resourcePath) {
        checkNotNull(workspaceId);
        checkNotNull(resourcePath);

        final Invocation request = getWebTarget().path(workspaceId)
                                                 .path(resourcePath)
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();
        return new SimpleRequest<Project>(request, DefaultProject.class, getAuthenticationManager());

    }

    /**
     * Retrieves project workspace {@link com.codenvy.client.model.Project}.
     *
     * @param workspaceId
     *         the workspace id.
     * @param projectReference
     *         the project reference
     * @return the workspace {@link com.codenvy.client.model.Project}
     * @throws NullPointerException
     *         if workspaceId parameter is {@code null}.
     */
    public Request<Project> getProject(String workspaceId, ProjectReference projectReference) {
        checkNotNull(workspaceId);
        checkNotNull(projectReference);

        final Invocation request = getWebTarget().path(workspaceId)
                                                 .path(projectReference.name())
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();
        return new SimpleRequest<Project>(request, DefaultProject.class, getAuthenticationManager());

    }

    /**
     * Imports a {@link com.codenvy.client.model.Project} in the given workspace.
     *
     * @param workspaceId
     *         the workspace id.
     * @param name
     *         the name of the project to import.
     * @param configurationPath
     *         the path to the configuration file
     * @return the new {@link com.codenvy.client.model.ProjectReference}, never {@code null}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<ImportResponse> importProject(String workspaceId, String name, Path configurationPath) {
        JsonValue jsonValue = getJsonObject(configurationPath);

        final Invocation request = getWebTarget().path(workspaceId)
                                                 .path("import")
                                                 .path(name)
                                                 .queryParam("force", "false")
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(json(jsonValue));

        return new SimpleRequest<ImportResponse>(request, DefaultImportResponse.class, getAuthenticationManager());
    }

    /**
     * Update project description of a {@link com.codenvy.client.model.Project} in the given workspace.
     *
     * @param projectReference
     *         the project reference
     * @param configurationPath
     *         the path to the configuration file
     * @return the new {@link com.codenvy.client.model.ProjectReference}, never {@code null}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<Project> updateProject(ProjectReference projectReference, Path configurationPath) {
        checkNotNull(projectReference);

        JsonValue projectNode = getJsonObject(configurationPath).get("project");

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path(projectReference.name())
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPut(json(projectNode));
        return new SimpleRequest<Project>(request, DefaultProject.class, getAuthenticationManager());

    }

    /**
     * Creates a {@link com.codenvy.client.model.ProjectReference} in the given workspace.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference} to create.
     * @return the new {@link com.codenvy.client.model.ProjectReference}, never {@code null}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<Project> create(ProjectReference projectReference) {

        checkNotNull(projectReference);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .queryParam("name", projectReference.name())
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(json(projectReference));

        return new SimpleRequest<Project>(request, DefaultProject.class, getAuthenticationManager());
    }


    /**
     * Switch visibility for a {@link com.codenvy.client.model.ProjectReference} in the given workspace.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference} to change visibility.
     * @param visibility
     *         the {@link com.codenvy.client.model.Visibility} attribute to change visibility.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<Void> switchVisibility(ProjectReference projectReference, Visibility visibility) {
        checkNotNull(projectReference);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("switch_visibility")
                                                 .path(projectReference.name())
                                                 .queryParam("visibility", visibility.name().toLowerCase())
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(json(projectReference));

        return new SimpleRequest<>(request, Void.class, getAuthenticationManager());
    }

    /**
     * Exports a resource in the given {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference}.
     * @param resourcePath
     *         the path of the resource to export, must be a folder.
     * @return the resource {@link ZipInputStream} or {@code null} if the resource is not found.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<ZipInputStream> exportResources(ProjectReference projectReference, String resourcePath) {
        checkNotNull(projectReference);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("export")
                                                 .path(projectReference.name())
                                                 .path(resourcePath == null ? "" : resourcePath)
                                                 .request()
                                                 .accept("application/zip")
                                                 .buildGet();

        return new RequestResponseAdaptor<>(new SimpleRequest<>(request, InputStream.class, getAuthenticationManager()),
                                            new Adaptor<ZipInputStream, InputStream>() {
                                                @Override
                                                public ZipInputStream adapt(InputStream response) {
                                                    return new ZipInputStream(response);
                                                }
                                            });
    }

    /**
     * Deletes a resource in the given {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference}.
     * @param resourcePath
     *         the path of the resource to delete.
     * @return the {@link Request} pointing to a {@link Void} result.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<Void> deleteResources(ProjectReference projectReference, String resourcePath) {
        checkNotNull(projectReference);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path(projectReference.name())
                                                 .path(resourcePath == null ? "" : resourcePath)
                                                 .request()
                                                 .buildDelete();

        return new SimpleRequest<>(request, Void.class, getAuthenticationManager());
    }

    /**
     * Upload a local ZIP folder.
     *
     * @param workspaceId
     *         the workspace id in which the ZIP folder will be imported.
     * @param projectReference
     *         the pre-existing {@link com.codenvy.client.model.ProjectReference} in which the archive content should be imported.
     * @param archiveInputStream
     *         the archive {@link InputStream}.
     * @return the {@link Request} pointing to a {@link Void} result.
     * @throws NullPointerException
     *         if workspaceId, projectName or archiveInputStream parameters are {@code null}.
     */
    @Override
    public Request<Void> importArchive(String workspaceId, ProjectReference projectReference, InputStream archiveInputStream) {
        checkNotNull(workspaceId);
        checkNotNull(projectReference);
        checkNotNull(archiveInputStream);

        final Invocation request = getWebTarget().path(workspaceId)
                                                 .path("import")
                                                 .path(projectReference.name())
                                                 .request()
                                                 .buildPost(entity(archiveInputStream, "application/zip"));

        return new SimpleRequest<>(request, Void.class, getAuthenticationManager());
    }

    /**
     * Updates a resource in the given {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference}.
     * @param filePath
     *         the path to the file to update.
     * @param fileInputStream
     *         the file {@link InputStream}.
     * @throws NullPointerException
     *         if project, filePath or fileInputStream parameter is {@code null}.
     */
    @Override
    public Request<Void> updateFile(ProjectReference projectReference, String filePath, InputStream fileInputStream) {
        checkNotNull(projectReference);
        checkNotNull(filePath);
        checkNotNull(fileInputStream);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("file")
                                                 .path(projectReference.name())
                                                 .path(filePath)
                                                 .request()
                                                 .buildPut(text(fileInputStream));

        return new SimpleRequest<>(request, Void.class, getAuthenticationManager());
    }

    /**
     * Gets file content in the given {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference}.
     * @param filePath
     *         the file path.
     * @return the file {@link InputStream} or {@code null} if not found.
     */
    @Override
    public Request<InputStream> getFile(ProjectReference projectReference, String filePath) {
        checkNotNull(projectReference);
        checkNotNull(filePath);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("file")
                                                 .path(projectReference.name())
                                                 .path(filePath)
                                                 .request()
                                                 .accept(TEXT_PLAIN)
                                                 .buildGet();

        return new SimpleRequest<>(request, InputStream.class, getAuthenticationManager());
    }

    /**
     * Returns if the given file exists in the given {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference}.
     * @param filePath
     *         the file path.
     * @return {@code true} if the given resource exists in the Codenvy project, {@code false} otherwise.
     * @throws NullPointerException
     *         if project or resourcePath parameter is {@code null}.
     */
    @Override
    public Request<Boolean> hasFile(ProjectReference projectReference, String filePath) {
        checkNotNull(projectReference);
        checkNotNull(filePath);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("file")
                                                 .path(projectReference.name())
                                                 .path(filePath)
                                                 .request()
                                                 .build("HEAD");

        return new RequestResponseAdaptor<>(new SimpleRequest<>(request, Response.class, getAuthenticationManager()),
                                            new Adaptor<Boolean, Response>() {
                                                @Override
                                                public Boolean adapt(Response response) {
                                                    // If status is Not found, it's not there else it's there
                                                    final Status status = fromStatusCode(response.getStatus());
                                                    return status != Status.NOT_FOUND;
                                                }
                                            });
    }


    /**
     * Returns if the given folder exists in the given {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference}.
     * @param folderPath
     *         the folder path.
     * @return {@code true} if the given resource exists in the Codenvy project, {@code false} otherwise.
     * @throws NullPointerException
     *         if project or resourcePath parameter is {@code null}.
     */
    @Override
    public Request<Boolean> hasFolder(ProjectReference projectReference, String folderPath) {
        checkNotNull(projectReference);
        checkNotNull(folderPath);

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path("children")
                                                 .path(projectReference.name())
                                                 .path(folderPath)
                                                 .request()
                                                 .build("HEAD");

        return new RequestResponseAdaptor<>(new SimpleRequest<>(request, Response.class, getAuthenticationManager()),
                                            new Adaptor<Boolean, Response>() {
                                                @Override
                                                public Boolean adapt(Response response) {
                                                    // If status is Not found, it's not there else it's there
                                                    final Status status = fromStatusCode(response.getStatus());
                                                    return status != Status.NOT_FOUND;
                                                }
                                            });
    }

    /**
     * Reads the JSON from the given path
     * @param configurationPath the path to the configuration file
     * @return JSON object that has been read from that file
     */
    protected JsonObject getJsonObject(Path configurationPath) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(configurationPath, Charset.defaultCharset())) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read the content of the configuration file '" + configurationPath + "'.", e);
        }

        JsonReader jSonReader = Json.createReader(new StringReader(stringBuilder.toString()));
        return jSonReader.readObject();
    }
}
