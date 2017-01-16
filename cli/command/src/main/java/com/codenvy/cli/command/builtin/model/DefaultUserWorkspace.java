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
package com.codenvy.cli.command.builtin.model;

import com.codenvy.cli.command.builtin.MultiRemoteCodenvy;
import com.codenvy.client.Codenvy;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.Workspace;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link com.codenvy.cli.command.builtin.model.UserWorkspace} that allow to have relationship between project and
 * workspaces.
 *
 * @author Florent Benoit
 */
public class DefaultUserWorkspace implements UserWorkspace {

    /**
     * Helper.
     */
    private MultiRemoteCodenvy multiRemoteCodenvy;

    /**
     * Remote instance.
     */
    private String remote;

    /**
     * Codenvy object used to interact with remote API.
     */
    private Codenvy codenvy;

    /**
     * Workspace ref object used to extract the data
     */
    private Workspace linkWorkspace;

    /**
     * Build a new workspace with a link to the remote api and workspace ref
     *
     * @param codenvy
     *         codenvy object
     * @param workspace
     *         the given workspace reference
     */
    public DefaultUserWorkspace(String remote, MultiRemoteCodenvy multiRemoteCodenvy, Codenvy codenvy, Workspace workspace) {
        this.remote = remote;
        this.multiRemoteCodenvy = multiRemoteCodenvy;
        this.codenvy = codenvy;
        this.linkWorkspace = workspace;
    }

    /**
     * @return identifier of the workspace
     */
    @Override
    public String id() {
        return linkWorkspace.id();
    }

    /**
     * @return the name of the workspace
     */
    @Override
    public String name() {
        return linkWorkspace.name();
    }

    /**
     * @return projects that are in this workspace
     */
    @Override
    public List<UserProjectReference> getProjects() {
        List<ProjectReference> readProjects = codenvy.project().getWorkspaceProjects(id()).execute();
        List<UserProjectReference> projects = new ArrayList<>();
        for (ProjectReference readProject : readProjects) {
            UserProjectReference userProjectReference = new DefaultUserProjectReference(codenvy, readProject, this);
            projects.add(userProjectReference);
        }
        return projects;
    }

    /**
     * @return remote of this workspace
     */
    @Override
    public String getRemote() {
        return remote;
    }

    public MultiRemoteCodenvy getMultiRemoteCodenvy() {
        return multiRemoteCodenvy;
    }

    @Override
    public Codenvy getCodenvy() {
        return codenvy;
    }


}
