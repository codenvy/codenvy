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

import com.codenvy.client.model.git.Log;
import com.codenvy.client.model.git.Revision;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Implementation of {@link com.codenvy.client.model.git.Log}
 * @author Florent Benoit
 */
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultLog implements Log {

    private List<Revision> commits;

    @JsonCreator
    public DefaultLog(@JsonProperty("commits") List<DefaultRevision> commits) {
        this.commits = ImmutableList.copyOf(commits == null ? new ArrayList<Revision>() : commits);
    }


    /**
     * @return list of commits
     */
    @Override
    public List<Revision> getCommits() {
        return commits;
    }
}
