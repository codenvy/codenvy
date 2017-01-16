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

import com.codenvy.client.model.git.Revision;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Implementation of @{link Revision}
 * @author Florent Benoit
 */
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultRevision implements Revision {

    /**
     * Name of the branch
     */
    private String branch;

    /**
     * Id of the revision
     */
    private String id;

    /**
     * Message of the revision
     */
    private String message;

    /**
     * Commit time of the revision
     */
    private long commitTime;

    /**
     * Creates a default revision object
     * @param branch Name of the branch
     * @param id Id of the revision
     * @param message Message of the revision
     * @param commitTime Commit time of the revision
     */
    @JsonCreator
    public DefaultRevision(@JsonProperty("branch") String branch,
                           @JsonProperty("id") String id,
                           @JsonProperty("message") String message,
                           @JsonProperty("commitTime") long commitTime) {
        this.branch = branch;
        this.id = id;
        this.message = message;
        this.commitTime = commitTime;

    }


    /**
     * @return branch name
     */
    @Override
    public String getBranch() {
        return branch;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * @return commit message
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * @return time of commit
     */
    @Override
    public long getCommitTime() {
        return commitTime;
    }
}
