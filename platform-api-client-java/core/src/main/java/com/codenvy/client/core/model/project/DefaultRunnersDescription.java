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
package com.codenvy.client.core.model.project;

import com.codenvy.client.model.project.RunnerConfiguration;
import com.codenvy.client.model.project.RunnersDescription;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Defines default Runner and all runners configuration.
 * @author Florent Benoit
 */
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultRunnersDescription implements RunnersDescription {

    private String defaultRunner;
    private Map<String, RunnerConfiguration> configurations;

    @JsonCreator
    public DefaultRunnersDescription(
            @JsonProperty("default") String defaultRunner,
            @JsonProperty("configs") Map<String, DefaultRunnerConfiguration> configurations) {
        this.defaultRunner = defaultRunner;
        this.configurations = ImmutableMap.copyOf(configurations == null ? new HashMap<String, RunnerConfiguration>() : configurations);
    }

    /**
     * @return the default runner selected (if any)
     */
    @JsonProperty("default")
    @Override
    public String defaultRunner() {
        return defaultRunner;
    }

    /**
     * Gets runner configuration by each configuration name.
     *
     * @return all available runner configurations
     */
    @JsonProperty("configs")
    @Override
    public Map<String, RunnerConfiguration> configurations() {
        return configurations;
    }
}
