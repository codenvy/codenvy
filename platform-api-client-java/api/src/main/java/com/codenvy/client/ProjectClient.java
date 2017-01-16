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

import com.codenvy.client.model.Project;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.Visibility;
import com.codenvy.client.model.project.ImportResponse;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * Client API for dealing with Project REST API.
 *
 * @author Florent Benoit
 */
public interface ProjectClient {
    /**
     * Retrieves all project references {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param workspaceId
     *         the workspace id.
     * @return the workspace {@link com.codenvy.client.model.ProjectReference} list never {@code null}.
     * @throws NullPointerException
     *         if workspaceId parameter is {@code null}.
     */
    Request<List<ProjectReference>> getWorkspaceProjects(String workspaceId);

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
    Request<Project> getProject(String workspaceId, String resourcePath);

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
    Request<Project> getProject(String workspaceId, ProjectReference projectReference);

    /**
     * Creates a {@link com.codenvy.client.model.Project} in the given workspace.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.Project} to create.
     * @return the new {@link com.codenvy.client.model.ProjectReference}, never {@code null}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<Project> create(ProjectReference projectReference);

    /**
     * Imports a {@link com.codenvy.client.model.Project} in the given workspace.
     *
     * @param workspaceId
     *         the workspace id.
     * @param name
     *         the name of the project to import.
     * @param configurationPath the path to the configuration file
     * @return the new {@link com.codenvy.client.model.project.ImportResponse}, never {@code null}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<ImportResponse> importProject(String workspaceId, String name, Path configurationPath);

    /**
     * Update project description of a {@link com.codenvy.client.model.Project} in the given workspace.
     *
     * @param projectReference
     *         the project reference
     * @param configurationPath the path to the configuration file
     * @return the new {@link com.codenvy.client.model.ProjectReference}, never {@code null}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<Project> updateProject(ProjectReference projectReference, Path configurationPath);

    /**
     * Exports a resource in the given {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference}.
     * @param resourcePath
     *         the path of the resource to export, must be a folder.
     * @return the resource {@link java.util.zip.ZipInputStream} or {@code null} if the resource is not found.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<ZipInputStream> exportResources(ProjectReference projectReference, String resourcePath);

    /**
     * Deletes a resource in the given {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference}.
     * @param resourcePath
     *         the path of the resource to delete.
     * @return the {@link com.codenvy.client.Request} pointing to a {@link Void} result.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<Void> deleteResources(ProjectReference projectReference, String resourcePath);

    /**
     * Upload a local ZIP folder.
     *
     * @param workspaceId
     *         the workspace id in which the ZIP folder will be imported.
     * @param projectReference
     *         the pre-exisiting {@link com.codenvy.client.model.ProjectReference} in which the archive content should be imported.
     * @param archiveInputStream
     *         the archive {@link java.io.InputStream}.
     * @return the {@link com.codenvy.client.Request} pointing to a {@link Void} result.
     * @throws NullPointerException
     *         if workspaceId, projectName or archiveInputStrem parameters are {@code null}.
     */
    Request<Void> importArchive(String workspaceId, ProjectReference projectReference, InputStream archiveInputStream);

    /**
     * Updates a resource in the given {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference}.
     * @param filePath
     *         the path to the file to update.
     * @param fileInputStream
     *         the file {@link java.io.InputStream}.
     * @throws NullPointerException
     *         if project, filePath or fileInputStream parameter is {@code null}.
     */
    Request<Void> updateFile(ProjectReference projectReference, String filePath, InputStream fileInputStream);

    /**
     * Gets file content in the given {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference}.
     * @param filePath
     *         the file path.
     * @return the file {@link java.io.InputStream} or {@code null} if not found.
     */
    Request<InputStream> getFile(ProjectReference projectReference, String filePath);

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
    Request<Boolean> hasFolder(ProjectReference projectReference, String folderPath);

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
    Request<Boolean> hasFile(ProjectReference projectReference, String filePath);

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
    Request<Void> switchVisibility(ProjectReference projectReference, Visibility visibility);
}
