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
import com.codenvy.client.model.runner.RunOptionsBuilder;

import java.util.List;

/**
 * Builder allowing to create and configure a {@link com.codenvy.client.model.runner.RunOptions} instance
 * @author Florent Benoit
 */
public class DefaultRunOptionsBuilder implements RunOptionsBuilder {

    /**
     * Define memory to use.
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
     * Specify the url of the project that needs to be created.
     *
     * @param memorySize
     *         the memory to use for this runner
     * @return {@link com.codenvy.client.model.runner.RunOptionsBuilder}
     */
    @Override
    public RunOptionsBuilder withMemorySize(int memorySize) {
        this.memorySize = memorySize;
        return this;
    }

    /**
     * Specify the name of the environment ID to use for this run.
     *
     * @param environmentId
     *         the name of the environment ID
     * @return {@link com.codenvy.client.model.runner.RunOptionsBuilder}
     */
    @Override
    public RunOptionsBuilder withEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
        return this;
    }

    /**
     * Specify the list of script files to use for this run.
     *
     * @param scriptFiles
     *         the list of script files
     * @return {@link com.codenvy.client.model.runner.RunOptionsBuilder}
     */
    @Override
    public RunOptionsBuilder withScriptFiles(List<String> scriptFiles) {
        this.scriptFiles = scriptFiles;
        return this;
    }

    /**
     * @return instance of {@link com.codenvy.client.model.runner.RunOptions}
     */
    @Override
    public RunOptions build() {
        return new DefaultRunOptions().withMemorySize(memorySize).withScriptFiles(scriptFiles).withEnvironmentId(environmentId);
    }
}
