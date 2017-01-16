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
package com.codenvy.client;

import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.RunnerStatus;
import com.codenvy.client.model.runner.RunOptions;

import java.util.List;

/**
 * @author Florent Benoit
 */
public interface RunnerClient {
    /**
     * Runs the given project with a codenvy runner.
     *
     * @param projectReference
     *         the project to run.
     * @return the {@link RunnerStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<RunnerStatus> run(ProjectReference projectReference);

    /**
     * Runs the given project with a codenvy runner.
     *
     * @param projectReference
     *         the project to run.
     * @param runOptions the options for the runner
     * @return the {@link RunnerStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<RunnerStatus> run(ProjectReference projectReference, RunOptions runOptions);

    /**
     * Stops the project runner with the given process id.
     *
     * @param projectReference
     *         the project.
     * @param processId
     *         the runner process id.
     * @return the {@link RunnerStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<RunnerStatus> stop(ProjectReference projectReference, long processId);

    /**
     * Gets the project runner status with the given process id.
     *
     * @param projectReference
     *         the project.
     * @param processId
     *         the runner process id.
     * @return the {@link RunnerStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<RunnerStatus> status(ProjectReference projectReference, long processId);

    /**
     * Gets the project runner logs with the given process id.
     *
     * @param projectReference
     *         the project.
     * @param processId
     *         the runner process id.
     * @return the runner logs.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<String> logs(ProjectReference projectReference, long processId);

    /**
     * Gets the project processes for the given project
     *
     * @param projectReference
     *         the project.
     * @return the different statuses.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<List<RunnerStatus>> processes(ProjectReference projectReference);


}
