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
package com.codenvy.client.dummy.project;

import com.codenvy.client.ProjectClient;
import com.codenvy.client.Request;
import com.codenvy.client.dummy.DummyRequest;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.Visibility;
import com.codenvy.client.model.project.ImportResponse;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;


/**
 * @author Florent Benoit
 */
public class DummyProjectClient implements ProjectClient {

    /**
     * Map between workspace ID and the list of projects
     */
    private Map<String, List<DummyProject>> projects;

    public DummyProjectClient() {
        this.projects = new HashMap<>();
    }


    public void registerProject(DummyProject dummyProject) {

        if (dummyProject.workspaceId() == null) {
            throw new IllegalStateException("The given project should have its workspace ID defined");
        }

        List<DummyProject> projectsPerWorkspace = projects.get(dummyProject.workspaceId());
        if (projectsPerWorkspace == null) {
            projectsPerWorkspace = new ArrayList<>();
            projects.put(dummyProject.workspaceId(), projectsPerWorkspace);
        }

        projectsPerWorkspace.add(dummyProject);
    }

    /**
     * Retrieves all project references {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param workspaceId
     *         the workspace id.
     * @return the workspace {@link com.codenvy.client.model.ProjectReference} list never {@code null}.
     * @throws NullPointerException
     *         if workspaceId parameter is {@code null}.
     */
    @Override
    public Request<List<ProjectReference>> getWorkspaceProjects(String workspaceId) {

        List<ProjectReference> projectReferences = new ArrayList<>();
        List<DummyProject> projectsPerWorkspace = projects.get(workspaceId);
        if (projectsPerWorkspace != null) {
            for (DummyProject dummyProject : projectsPerWorkspace) {
                projectReferences.add(dummyProject);
            }
        }
        return new DummyRequest<>(projectReferences);
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
        List<DummyProject> projectsPerWorkspace = projects.get(workspaceId);
        if (projectsPerWorkspace != null) {
            for (DummyProject dummyProject : projectsPerWorkspace) {
                if (dummyProject.name().equals(resourcePath)) {
                    return new DummyRequest<Project>(dummyProject);
                }
            }
        }
        return new DummyRequest<>(null);
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
    @Override
    public Request<Project> getProject(String workspaceId, ProjectReference projectReference) {
        List<DummyProject> projectsPerWorkspace = projects.get(workspaceId);
        for (DummyProject dummyProject : projectsPerWorkspace) {
            if (dummyProject.name().equals(projectReference.name())) {
                return new DummyRequest<Project>(dummyProject);
            }
        }
        return new DummyRequest<>(null);
    }

    /**
     * Creates a {@link com.codenvy.client.model.Project} in the given workspace.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.Project} to create.
     * @return the new {@link com.codenvy.client.model.ProjectReference}, never {@code null}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<Project> create(ProjectReference projectReference) {

        DummyProject dummyProject = new DummyProject(projectReference);
        List<DummyProject> projectsPerWorkspace = projects.get(projectReference.workspaceId());
        if (projectsPerWorkspace == null) {
            projectsPerWorkspace = new ArrayList<>();
            projects.put(projectReference.workspaceId(), projectsPerWorkspace);
        }
        projectsPerWorkspace.add(dummyProject);

        return new DummyRequest<Project>(dummyProject);

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
        return null;
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
        return null;
    }

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
    @Override
    public Request<ZipInputStream> exportResources(ProjectReference projectReference, String resourcePath) {
        return new DummyRequest<>(null);
    }

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
    @Override
    public Request<Void> deleteResources(ProjectReference projectReference, String resourcePath) {
        return new DummyRequest<>(null);
    }

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
    @Override
    public Request<Void> importArchive(String workspaceId, ProjectReference projectReference, InputStream archiveInputStream) {
        return new DummyRequest<>(null);
    }

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
    @Override
    public Request<Void> updateFile(ProjectReference projectReference, String filePath, InputStream fileInputStream) {
        return new DummyRequest<>(null);

    }

    /**
     * Gets file content in the given {@link com.codenvy.client.model.ProjectReference}.
     *
     * @param projectReference
     *         the {@link com.codenvy.client.model.ProjectReference}.
     * @param filePath
     *         the file path.
     * @return the file {@link java.io.InputStream} or {@code null} if not found.
     */
    @Override
    public Request<InputStream> getFile(ProjectReference projectReference, String filePath) {
        return new DummyRequest<>(null);
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
        return new DummyRequest<>(null);
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
        return new DummyRequest<>(null);
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
        return new DummyRequest<>(null);
    }

}
