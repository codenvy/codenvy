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

/**
 * The Codenvy API client.
 *
 * @author Florent Benoit
 */
public interface Codenvy {

    /**
     * Returns the user API client.
     *
     * @return the user API client.
     */
    UserClient user();

    /**
     * Returns the builder API client.
     *
     * @return the builder API client.
     */

    BuilderClient builder();

    /**
     * Returns the runner API client.
     *
     * @return the runner API client.
     */
    RunnerClient runner();

    /**
     * Returns the project API client.
     *
     * @return the project API client.
     */
    ProjectClient project();

    /**
     * Returns the workspace API client.
     *
     * @return the workspace API client.
     */
    WorkspaceClient workspace();

    /**
     * Returns the version API client.
     *
     * @return the version API client.
     */
    VersionClient version();

    /**
     * Returns the factory API client.
     *
     * @return the factory API client.
     */
    FactoryClient factory();

    /**
     * Returns the Git API client.
     *
     * @return the Git API client.
     */
    GitClient git();

    /**
     * Returns the factory API client.
     *
     * @return the factory API client.
     */
    VFSClient vfs();

}
