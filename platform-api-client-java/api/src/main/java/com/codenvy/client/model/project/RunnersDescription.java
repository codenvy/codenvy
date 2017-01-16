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
package com.codenvy.client.model.project;

import java.util.Map;

/**
 * Defines default Runner and all runners configuration.
 * @author Florent Benoit
 */
public interface RunnersDescription {

    /**
     * @return the default runner selected (if any)
     */
    String defaultRunner();

    /**
     * Gets runner configuration by each configuration name.
     * @return all available runner configurations
     */
    Map<String, RunnerConfiguration> configurations();
}
