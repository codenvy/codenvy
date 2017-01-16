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
package com.codenvy.client.core.model.git;

import com.codenvy.client.model.git.InitRequest;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Implementation of {@link com.codenvy.client.model.git.InitRequest}
 * @author Florent Benoit
 */
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultInitRequest implements InitRequest {

    /**
     * Bare repository ?
     */
    private boolean bare;

    /**
     * Initial Commit ?
     */
    private boolean initCommit;

    /**
     * Working directory
     */
    private String workingDir;

    /**
     * Creates a default init request object
     *
     * @param workingDir
     *         Name of the branch
     * @param bare
     *         Message of the revision
     * @param initCommit
     *         Commit time of the revision
     */
    @JsonCreator
    public DefaultInitRequest(@JsonProperty("workingDir") String workingDir,
                           @JsonProperty("bare") boolean bare,
                           @JsonProperty("initCommit") boolean initCommit) {
        this.workingDir = workingDir;
        this.bare = bare;
        this.initCommit = initCommit;
    }


    /**
     * @return the working directory
     */
    @Override
    public String getWorkingDir() {
        return workingDir;
    }

    /**
     * @return true if the git repository should be a bare repository
     */
    @Override
    public boolean isBare() {
        return bare;
    }

    /**
     * @return true if it's the initial commit
     */
    @Override
    public boolean isInitCommit() {
        return initCommit;
    }

}
