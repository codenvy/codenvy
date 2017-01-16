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
package com.codenvy.client.core.model.runner;

import com.codenvy.client.model.runner.RunOptions;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Florent Benoit
 */
public class DefaultRunOptions implements RunOptions {

    /**
     * Memory size.
     */
    private int memorySize;

    /**
     * Specify the name of the environment ID to use for this run.
     */
    private String environmentId;

    /**
     * Specify the list of script files to use for this run.
     */
    private List<String> scriptFiles;

    /**
     * @return in MB the size of the memory to use for this runner
     */
    @Override
    @JsonProperty("memorySize")
    public int getMemorySize() {
        return memorySize;
    }

    /**
     * @return id of environment that should be used for running an application.
     */
    @Override
    @JsonProperty("environmentId")
    public String getEnvironmentId() {
        return environmentId;
    }

    /**
     * @return recipes for the runner
     */
    @JsonProperty("scriptFiles")
    @Override
    public List<String> getScriptFiles() {
        return scriptFiles;
    }

    /**
     * Specify the url of the project that needs to be created.
     *
     * @param memorySize
     *         the memory to use for this runner
     * @return {@link com.codenvy.client.core.model.runner.DefaultRunOptions}
     */
    public DefaultRunOptions withMemorySize(int memorySize) {
        this.memorySize = memorySize;
        return this;
    }

    /**
     * Specify the name of the environment ID to use for this run.
     *
     * @param environmentId
     *         the name of the environment ID
     * @return {@link com.codenvy.client.core.model.runner.DefaultRunOptions}
     */
    public DefaultRunOptions withEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
        return this;
    }

    /**
     * Specify the list of script files to use for this run.
     *
     * @param scriptFiles
     *         the list of script files
     * @return {@link com.codenvy.client.core.model.runner.DefaultRunOptions}
     */
    public DefaultRunOptions withScriptFiles(List<String> scriptFiles) {
        this.scriptFiles = scriptFiles;
        return this;
    }
}
