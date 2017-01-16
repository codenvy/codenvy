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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the configuration for a given runner.
 *
 * @author Florent Benoit
 */
public class DefaultRunnerConfiguration implements RunnerConfiguration {

    private int ram;
    private Map<String, String> options;
    private Map<String, String> variables;

    @JsonCreator
    public DefaultRunnerConfiguration(
            @JsonProperty("ram") int ram,
            @JsonProperty("options") Map<String, String> options,
            @JsonProperty("variables") Map<String, String> variables) {
        this.ram = ram;
        this.options = ImmutableMap.copyOf(options == null ? new HashMap<String, String>() : options);
        this.variables = ImmutableMap.copyOf(variables == null ? new HashMap<String, String>() : variables);
    }

    /**
     * @return amount of RAM for this configuration in megabytes.
     */
    @Override
    @JsonProperty("ram")
    public int ram() {
        return ram;
    }

    /**
     * @return runtime options (runner type and(or) receipt specific).
     */
    @JsonProperty("options")
    @Override
    public Map<String, String> options() {
        return options;
    }

    /**
     * @return environment variables (runner type and(or) receipt specific).
     */
    @Override
    @JsonProperty("variables")
    public Map<String, String> variables() {
        return variables;
    }
}
