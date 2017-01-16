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
package com.codenvy.client.model.runner;

import java.util.List;

/**
 * The Codenvy Runner options builder.
 *
 * @author Florent Benoit
 */
public interface RunOptionsBuilder {

    /**
     * Specify the url of the project that needs to be created.
     *
     * @param memorySize the memory to use for this runner
     * @return {@link com.codenvy.client.model.runner.RunOptionsBuilder}
     */
    RunOptionsBuilder withMemorySize(int memorySize);

    /**
     * Specify the name of the environment ID to use for this run.
     *
     * @param environmentId the name of the environment ID
     * @return {@link com.codenvy.client.model.runner.RunOptionsBuilder}
     */
    RunOptionsBuilder withEnvironmentId(String environmentId);

    /**
     * Specify the list of script files to use for this run.
     *
     * @param scriptFiles the list of script files
     * @return {@link com.codenvy.client.model.runner.RunOptionsBuilder}
     */
    RunOptionsBuilder withScriptFiles(List<String> scriptFiles);

    /**
     * @return instance of {@link com.codenvy.client.model.runner.RunOptions}
     */
    RunOptions build();
}
