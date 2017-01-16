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

import com.codenvy.client.model.BuilderState;
import com.codenvy.client.model.BuilderStatus;
import com.codenvy.client.model.Link;
import com.codenvy.client.model.builder.BuilderMetric;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.codenvy.client.core.model.DefaultLink.DOWNLOAD_LINK_REL_ATTRIBUTE_VALUE;

/**
 * The codenvy runner object model.
 *
 * @author Kevin Pollet
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultBuilderStatus implements BuilderStatus {
    private final long                taskId;
    private final long                startTime;
    private final BuilderState        status;
    private final List<Link>          links;
    private final List<BuilderMetric> builderMetrics;

    @JsonCreator
    public DefaultBuilderStatus(@JsonProperty("taskId") long taskId,
                                @JsonProperty("startTime") long startTime,
                                @JsonProperty("status") BuilderState status,
                                @JsonProperty("links") List<DefaultLink> links,
                                @JsonProperty("buildStats") List<DefaultMetric> builderMetrics
                               ) {
        this.taskId = taskId;
        this.startTime = startTime;
        this.status = status;
        this.links = ImmutableList.copyOf(links == null ? new ArrayList<Link>() : links);
        this.builderMetrics = ImmutableList.copyOf(builderMetrics == null ? new ArrayList<BuilderMetric>() : builderMetrics);
    }

    /**
     * Gets the build result download {@link DefaultLink}.
     *
     * @return the download {@link DefaultLink}.
     */
    public Link getDownloadLink() {
        for (Link oneLink : links) {
            if (DOWNLOAD_LINK_REL_ATTRIBUTE_VALUE.equals(oneLink.rel())) {
                return oneLink;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "CodenvyBuilderStatus [taskId=" + taskId + ", startTime=" + startTime + ", status=" + status + ", links=" + links +
               ", buildStats=" + builderMetrics + "]";
    }

    /**
     * @return statistics for the runner process on the build part
     */
    @Override
    public List<BuilderMetric> getBuildStats() {
        return builderMetrics;
    }

    @Override
    public long taskId() {
        return taskId;
    }

    @Override
    public long startTime() {
        return startTime;
    }

    @Override
    public BuilderState status() {
        return status;
    }

    @Override
    public List<Link> links() {
        return links;
    }
}
