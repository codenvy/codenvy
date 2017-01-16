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

/**
 * Metrics are used for example in runners and builders.
 *
 * @author Florent Benoit
 */
public interface Metric {

    /**
     * Name of the metric.
     */
    String getName();

    /**
     * Value of the metric.
     */
    String getValue();

    /**
     * Description of the metric.
     */
    String getDescription();
}
