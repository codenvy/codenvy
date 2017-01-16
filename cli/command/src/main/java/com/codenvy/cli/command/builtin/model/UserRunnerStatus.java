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
package com.codenvy.cli.command.builtin.model;

import com.codenvy.client.model.RunnerStatus;

/**
 * Represents runner status and its local identifier
 * @author Florent Benoit
 */
public interface UserRunnerStatus {

    /**
     * @return complete ID
     */
    String sha1ID();

    /**
     * @return a short identifier that could identify the project
     */
    String shortId();

    /**
     * @return the linked project
     */
    UserProjectReference getProject();

    /**
     * @return inner status
     */
    RunnerStatus getInnerStatus();

}
