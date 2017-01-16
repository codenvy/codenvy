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
package com.codenvy.client.model;

/**
 * The Codenvy project builder.
 *
 * @author Kevin Pollet
 */
public interface ProjectBuilder {

    /**
     * Specify the url of the project that needs to be created.
     *
     * @param url
     *         the project URL
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    ProjectBuilder withUrl(String url);

    /**
     * Specify the visibility of the project that needs to be created.
     *
     * @param visibility
     *         the project visibility
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    ProjectBuilder withVisibility(String visibility);

    /**
     * Specify the Project ID of the project that needs to be created.
     *
     * @param projectTypeId
     *         the project Type ID
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    ProjectBuilder withType(String projectTypeId);

    /**
     * Specify the workspace ID of the project that needs to be created.
     *
     * @param workspaceId
     *         the project's workspace ID
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    ProjectBuilder withWorkspaceId(String workspaceId);

    /**
     * Specify the project Type name of the project that needs to be created.
     *
     * @param projectTypeName
     *         the project type name
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    ProjectBuilder withTypeName(String projectTypeName);

    /**
     * Specify the name of the project that needs to be created.
     *
     * @param name
     *         the project's name
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    ProjectBuilder withName(String name);

    /**
     * Specify the path of the project that needs to be created.
     *
     * @param path
     *         the project's path
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    ProjectBuilder withPath(String path);

    /**
     * Specify the description of the project that needs to be created.
     *
     * @param description
     *         the project's description
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    ProjectBuilder withDescription(String description);

    /**
     * Specify the workspace name of the project that needs to be created.
     *
     * @param workspaceName
     *         the project's workspace name
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    ProjectBuilder withWorkspaceName(String workspaceName);

    /**
     * Specify the IDE URL of the project that needs to be created.
     *
     * @param ideUrl
     *         the project's IDE url
     * @return {@link com.codenvy.client.model.ProjectBuilder}
     */
    ProjectBuilder withIdeUrl(String ideUrl);

    /**
     * @return instance of {@link com.codenvy.client.model.ProjectReference}
     */
    ProjectReference build();
}
