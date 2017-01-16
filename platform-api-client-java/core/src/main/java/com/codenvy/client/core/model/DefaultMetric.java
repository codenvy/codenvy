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

import com.codenvy.client.model.builder.BuilderMetric;
import com.codenvy.client.model.runner.RunnerMetric;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metric used in Builders and Runners.
 *
 * @author Florent Benoit
 */
public class DefaultMetric implements RunnerMetric, BuilderMetric {

    /**
     * Name of the metric.
     */
    private final String name;

    /**
     * Value of the metric.
     */
    private final String value;

    /**
     * Description of the metric.
     */
    private final String description;

    /**
     * Constructs an instance of both {@link com.codenvy.client.model.runner.RunnerMetric} and {@link
     * com.codenvy.client.model.builder.BuilderMetric}.
     */
    @JsonCreator
    public DefaultMetric(
            @JsonProperty("name") String name,
            @JsonProperty("value") String value,
            @JsonProperty("description") String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
