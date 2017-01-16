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
package com.codenvy.client.core.model;

import com.codenvy.client.model.Link;
import com.codenvy.client.model.RunnerState;
import com.codenvy.client.model.RunnerStatus;
import com.codenvy.client.model.builder.BuilderMetric;
import com.codenvy.client.model.runner.RunnerMetric;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.codenvy.client.core.model.DefaultLink.WEB_LINK_REL_ATTRIBUTE_VALUE;

/**
 * The codenvy runner object model.
 *
 * @author Kevin Pollet
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultRunnerStatus implements RunnerStatus {
    private final long                stopTime;
    private final String              debugHost;
    private final long                debugPort;
    private final long                processId;
    private final long                startTime;
    private final RunnerState         status;
    private final List<Link>          links;
    private final List<RunnerMetric>  runnerMetrics;
    private final List<BuilderMetric> builderMetrics;

    @JsonCreator
    public DefaultRunnerStatus(@JsonProperty("stopTime") long stopTime,
                               @JsonProperty("debugHost") String debugHost,
                               @JsonProperty("debugPort") long debugPort,
                               @JsonProperty("processId") long processId,
                               @JsonProperty("startTime") long startTime,
                               @JsonProperty("status") RunnerState status,
                               @JsonProperty("links") List<DefaultLink> links,
                               @JsonProperty("runStats") List<DefaultMetric> runnerMetrics,
                               @JsonProperty("buildStats") List<DefaultMetric> builderMetrics
                              ) {
        this.stopTime = stopTime;
        this.debugHost = debugHost;
        this.debugPort = debugPort;
        this.processId = processId;
        this.startTime = startTime;
        this.status = status;
        this.links = ImmutableList.copyOf(links == null ? new ArrayList<Link>() : links);
        this.runnerMetrics = ImmutableList.copyOf(runnerMetrics == null ? new ArrayList<RunnerMetric>() : runnerMetrics);
        this.builderMetrics = ImmutableList.copyOf(builderMetrics == null ? new ArrayList<BuilderMetric>() : builderMetrics);
    }

    @Override
    public long stopTime() {
        return stopTime;
    }

    @Override
    public String debugHost() {
        return debugHost;
    }

    @Override
    public long debugPort() {
        return debugPort;
    }

    @Override
    public long processId() {
        return processId;
    }

    @Override
    public long startTime() {
        return startTime;
    }

    @Override
    public RunnerState status() {
        return status;
    }

    @Override
    public List<Link> links() {
        return links;
    }


    /**
     * Returns the web {@link DefaultLink}.
     *
     * @return the web {@link DefaultLink} or {@code null}.
     */
    @Override
    public Link getWebLink() {
        for (Link oneLink : links) {
            if (WEB_LINK_REL_ATTRIBUTE_VALUE.equalsIgnoreCase(oneLink.rel())) {
                return oneLink;
            }
        }
        return null;
    }

    /**
     * @return statistics for the runner process
     */
    @Override
    public List<RunnerMetric> getRunStats() {
        return runnerMetrics;
    }

    /**
     * @return statistics for the runner process on the build part
     */
    @Override
    public List<BuilderMetric> getBuildStats() {
        return builderMetrics;
    }

    @Override
    public String toString() {
        return "CodenvyRunnerStatus [stopTime=" + stopTime + ", debugHost=" + debugHost + ", debugPort=" + debugPort + ", processId="
               + processId + ", startTime=" + startTime + ", status=" + status + ", links=" + links + ", runStats=" + runnerMetrics +
               ", buildStats=" + builderMetrics + "]";
    }

}
