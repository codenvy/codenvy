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
package com.codenvy.client.dummy.builder;

import com.codenvy.client.model.BuilderState;
import com.codenvy.client.model.BuilderStatus;
import com.codenvy.client.model.Link;
import com.codenvy.client.model.builder.BuilderMetric;

import java.util.List;

/**
 * @author Florent Benoit
 */
public class DummyBuilderStatus implements BuilderStatus {

    private long                taskId;
    private long                startTime;
    private BuilderState        status;
    private List<Link>          links;
    private List<BuilderMetric> builderMetrics;
    private String log;

    public DummyBuilderStatus(long taskId) {
        this.taskId = taskId;
        this.status = BuilderState.IN_QUEUE;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public void setStatus(BuilderState state) {
        this.status = state;
    }

    /**
     * @return internal ID of this builder status
     */
    @Override
    public long taskId() {
        return taskId;
    }

    /**
     * @return start time of this builder
     */
    @Override
    public long startTime() {
        return startTime;
    }

    /**
     * @return {@link com.codenvy.client.model.BuilderState} status
     */
    @Override
    public BuilderState status() {
        return status;
    }

    /**
     * @return links of this runner
     */
    @Override
    public List<Link> links() {
        return links;
    }

    /**
     * Gets the build result download {@link com.codenvy.client.model.Link}.
     *
     * @return the download {@link com.codenvy.client.model.Link}.
     */
    @Override
    public Link getDownloadLink() {
        return null;
    }

    /**
     * @return statistics for the runner process on the build part
     */
    @Override
    public List<BuilderMetric> getBuildStats() {
        return builderMetrics;
    }

    public String getLog() {
        return log;
    }

}
