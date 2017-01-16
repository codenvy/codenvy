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

import java.util.List;

/**
 * The codenvy builder object model.
 *
 * @author Florent Benoit
 */
public interface BuilderStatus {

    /**
     * @return internal ID of this builder status
     */
    long taskId();

    /**
     * @return start time of this builder
     */
    long startTime();

    /**
     * @return {@link com.codenvy.client.model.BuilderState} status
     */
    BuilderState status();

    /**
     * @return links of this runner
     */
    List<Link> links();

    /**
     * Gets the build result download {@link Link}.
     *
     * @return the download {@link Link}.
     */
    Link getDownloadLink();

    /**
     * @return statistics for the runner process on the build part
     */
    List<BuilderMetric> getBuildStats();

}
