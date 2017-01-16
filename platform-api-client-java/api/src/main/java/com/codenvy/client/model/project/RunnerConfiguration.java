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
 * Defines the configuration for a given runner.
 *
 * @author Florent Benoit
 */
public interface RunnerConfiguration {

    /**
     * @return amount of RAM for this configuration in megabytes.
     */
    int ram();

    /**
     * @return runtime options (runner type and(or) receipt specific).
     */
    Map<String, String> options();

    /**
     * @return environment variables (runner type and(or) receipt specific).
     */
    Map<String, String> variables();

}
