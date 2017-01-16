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

import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.git.Log;
import com.codenvy.client.model.git.LogRequest;

/**
 * Client API for dealing with Git REST API.
 *
 * @author Florent Benoit
 */
public interface GitClient {

    /**
     * Retrieves a Git read-only URL for the given project.
     *
     * @param projectReference
     *         the project reference.
     * @return a GIT URL that allow to clone the given project.
     */
    Request<String> readOnlyUrl(ProjectReference projectReference);


    /**
     * Initialize a Git repository for the given project.
     *
     * @param projectReference
     *         the project reference.
     * @return nothing.
     */
    Request<Void> init(ProjectReference projectReference);

    /**
     * Get log data for the given
     *
     * @param projectReference
     *         the project reference.
     * @param logRequest
     *         the log request.
     * @return a log containing all revisions
     */
    Request<Log> log(ProjectReference projectReference, LogRequest logRequest);

}
