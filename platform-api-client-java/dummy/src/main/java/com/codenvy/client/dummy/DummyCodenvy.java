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
package com.codenvy.client.dummy;

import com.codenvy.client.Codenvy;
import com.codenvy.client.FactoryClient;
import com.codenvy.client.GitClient;
import com.codenvy.client.RunnerClient;
import com.codenvy.client.UserClient;
import com.codenvy.client.VFSClient;
import com.codenvy.client.VersionClient;
import com.codenvy.client.dummy.builder.DummyBuilderClient;
import com.codenvy.client.dummy.project.DummyProjectClient;
import com.codenvy.client.dummy.runner.DummyRunnerClient;
import com.codenvy.client.dummy.workspace.DummyWorkspaceClient;

/**
 * @author Florent Benoit
 */
public class DummyCodenvy implements Codenvy {

    private DummyBuilderClient   builderClient;
    private DummyRunnerClient   runnerClient;
    private DummyProjectClient   projectClient;
    private DummyWorkspaceClient workspaceClient;


    public DummyCodenvy() {
        this.builderClient = new DummyBuilderClient();
        this.projectClient = new DummyProjectClient();
        this.runnerClient = new DummyRunnerClient();
        this.workspaceClient = new DummyWorkspaceClient();

    }

    /**
     * Returns the user API client.
     *
     * @return the user API client.
     */
    @Override
    public UserClient user() {
        return null;
    }

    /**
     * Returns the builder API client.
     *
     * @return the builder API client.
     */
    @Override
    public DummyBuilderClient builder() {
        return builderClient;
    }

    /**
     * Returns the runner API client.
     *
     * @return the runner API client.
     */
    @Override
    public RunnerClient runner() {
        return runnerClient;
    }

    /**
     * Returns the project API client.
     *
     * @return the project API client.
     */
    @Override
    public DummyProjectClient project() {
        return projectClient;
    }

    /**
     * Returns the workspace API client.
     *
     * @return the workspace API client.
     */
    @Override
    public DummyWorkspaceClient workspace() {
        return workspaceClient;
    }

    /**
     * Returns the version API client.
     *
     * @return the version API client.
     */
    @Override
    public VersionClient version() {
        return null;
    }

    /**
     * Returns the factory API client.
     *
     * @return the factory API client.
     */
    @Override
    public FactoryClient factory() {
        return null;
    }

    /**
     * Returns the Git API client.
     *
     * @return the Git API client.
     */
    @Override
    public GitClient git() {
        return null;
    }

    /**
     * Returns the factory API client.
     *
     * @return the factory API client.
     */
    @Override
    public VFSClient vfs() {
        return null;
    }

}
