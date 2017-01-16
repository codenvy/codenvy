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
package com.codenvy.client.model;

import com.codenvy.client.model.builder.BuilderMetric;
import com.codenvy.client.model.runner.RunnerMetric;

import java.util.List;

/**
 * The codenvy runner object model.
 *
 * @author Florent Benoit
 */
public interface RunnerStatus {

    /**
     * @return Stop time of this runner
     */
    long stopTime();

    /**
     * @return debug hostname of this runner
     */
    String debugHost();

    /**
     * @return debug port of this runner
     */
    long debugPort();

    /**
     * @return internal process ID of this runner
     */
    long processId();

    /**
     * @return Start time of this runner
     */
    long startTime();

    /**
     * @return {@link com.codenvy.client.model.RunnerState} state of this runner
     */
    RunnerState status();

    /**
     * @return links of this runner
     */
    List<Link> links();

    /**
     * Returns the web {@link Link}.
     *
     * @return the web {@link Link} or {@code null}.
     */
    Link getWebLink();

    /**
     * @return statistics for the runner process
     */
    List<RunnerMetric> getRunStats();

    /**
     * @return statistics for the runner process on the build part
     */
    List<BuilderMetric> getBuildStats();

}
