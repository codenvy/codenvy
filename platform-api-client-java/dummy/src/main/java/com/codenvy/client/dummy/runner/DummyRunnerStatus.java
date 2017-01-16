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
package com.codenvy.client.dummy.runner;

import com.codenvy.client.model.Link;
import com.codenvy.client.model.RunnerState;
import com.codenvy.client.model.RunnerStatus;
import com.codenvy.client.model.builder.BuilderMetric;
import com.codenvy.client.model.runner.RunnerMetric;

import java.util.List;

/**
 * @author Florent Benoit
 */
public class DummyRunnerStatus implements RunnerStatus {

    private long                processId;
    private long                startTime;
    private RunnerState         state;
    private List<Link>          links;
    private List<BuilderMetric> builderMetrics;
    private String              log;

    public DummyRunnerStatus(long processId) {
        this.processId = processId;
        this.state = RunnerState.NEW;
    }



    /**
     * @return Stop time of this runner
     */
    @Override
    public long stopTime() {
        return 0;
    }

    /**
     * @return debug hostname of this runner
     */
    @Override
    public String debugHost() {
        return null;
    }

    /**
     * @return debug port of this runner
     */
    @Override
    public long debugPort() {
        return 0;
    }

    /**
     * @return internal process ID of this runner
     */
    @Override
    public long processId() {
        return processId;
    }

    /**
     * @return start time of this builder
     */
    @Override
    public long startTime() {
        return startTime;
    }

    /**
     * @return {@link com.codenvy.client.model.RunnerState} state of this runner
     */
    @Override
    public RunnerState status() {
        return state;
    }


    /**
     * @return links of this runner
     */
    @Override
    public List<Link> links() {
        return links;
    }

    /**
     * Returns the web {@link com.codenvy.client.model.Link}.
     *
     * @return the web {@link com.codenvy.client.model.Link} or {@code null}.
     */
    @Override
    public Link getWebLink() {
        return null;
    }

    /**
     * @return statistics for the runner process
     */
    @Override
    public List<RunnerMetric> getRunStats() {
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

    public void setLog(String log) {
        this.log = log;
    }

    public void setStatus(RunnerState state) {
        this.state = state;
    }
}
