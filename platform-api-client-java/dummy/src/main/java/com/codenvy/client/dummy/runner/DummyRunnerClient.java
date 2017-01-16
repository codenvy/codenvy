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
package com.codenvy.client.dummy.runner;

import com.codenvy.client.Request;
import com.codenvy.client.RunnerClient;
import com.codenvy.client.dummy.DummyRequest;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.RunnerState;
import com.codenvy.client.model.RunnerStatus;
import com.codenvy.client.model.runner.RunOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Florent Benoit
 */
public class DummyRunnerClient implements RunnerClient {

    /**
     * List of available runner status per project name.
     */
    private Map<String, List<DummyRunnerStatus>> availableRunnerStatuses;

    /**
     * List of available runner status per project name.
     */
    private Map<String, List<DummyRunnerStatus>> currentRunnerStatuses;

    /**
     * Current builder index
     */
    private int runnerStatusIndex = 0;


    public DummyRunnerClient() {
        this.availableRunnerStatuses = new HashMap<>();
        this.currentRunnerStatuses = new HashMap<>();
    }


    /**
     * Runs the given project with a codenvy runner.
     *
     * @param projectReference
     *         the project to run.
     * @return the {@link com.codenvy.client.model.RunnerStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<RunnerStatus> run(ProjectReference projectReference) {
        String projectName = projectReference.name();
        List<DummyRunnerStatus> statuses = availableRunnerStatuses.get(projectName);
        DummyRunnerStatus runnerStatus = null;
        if (statuses == null || statuses.isEmpty()) {
            runnerStatus = null;
        } else {
            runnerStatus = statuses.get(runnerStatusIndex);
            // add it to current process
            runnerStatus.setStatus(RunnerState.RUNNING);

            List<DummyRunnerStatus> currents = currentRunnerStatuses.get(projectName);
            if (currents == null) {
                currents = new ArrayList<>();
                currentRunnerStatuses.put(projectName, currents);
            }

            currents.add(runnerStatus);
        }
        return new DummyRequest<RunnerStatus>(runnerStatus);
    }

    /**
     * Runs the given project with a codenvy runner.
     *
     * @param projectReference
     *         the project to run.
     * @param runOptions
     *         the options for the runner
     * @return the {@link com.codenvy.client.model.RunnerStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<RunnerStatus> run(ProjectReference projectReference, RunOptions runOptions) {
        return run(projectReference);
    }

    /**
     * Stops the project runner with the given process id.
     *
     * @param projectReference
     *         the project.
     * @param processId
     *         the runner process id.
     * @return the {@link com.codenvy.client.model.RunnerStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<RunnerStatus> stop(ProjectReference projectReference, long processId) {
        List<DummyRunnerStatus> statuses = availableRunnerStatuses.get(projectReference.name());
        DummyRunnerStatus found = null;
        if (statuses != null && !statuses.isEmpty()) {
            // search matching status
            for (DummyRunnerStatus runnerStatus : statuses) {
                if (processId == runnerStatus.processId()) {
                    found = runnerStatus;
                }
            }
        }
        // cancel
        if (found != null) {
            found.setStatus(RunnerState.STOPPED);
            // remove if found
            List<DummyRunnerStatus> currents = currentRunnerStatuses.get(projectReference.name());
            if (currents != null) {
                currents.remove(found);
            }
        }


        return new DummyRequest<RunnerStatus>(found);
    }

    /**
     * Gets the status of the runner with the given process id.
     *
     * @param projectReference
     *         the project.
     * @param processId
     *         the runner process id.
     * @return the {@link com.codenvy.client.model.BuilderStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<RunnerStatus> status(ProjectReference projectReference, long processId) {

        List<DummyRunnerStatus> statuses = availableRunnerStatuses.get(projectReference.name());
        if (statuses != null && !statuses.isEmpty()) {
            // search matching status
            for (RunnerStatus runnerStatus : statuses) {
                if (processId == runnerStatus.processId()) {
                    return new DummyRequest<>(runnerStatus);
                }
            }
        }

        // not found
        return new DummyRequest<>(null);
    }

    /**
     * Gets the logs of the builder with the given task id.
     *
     * @param projectReference
     *         the project.
     * @param taskId
     *         the builder task id.
     * @return the builder logs.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<String> logs(ProjectReference projectReference, long taskId) {
        List<DummyRunnerStatus> statuses = availableRunnerStatuses.get(projectReference.name());
        if (statuses != null && !statuses.isEmpty()) {
            // search matching status
            for (DummyRunnerStatus runnerStatus : statuses) {
                if (taskId == runnerStatus.processId()) {
                    return new DummyRequest<>(runnerStatus.getLog());
                }
            }
        }
        // not found
        return new DummyRequest<>(null);
    }

    /**
     * Gets the project processes for the given project
     *
     * @param projectReference
     *         the project.
     * @return the different statuses.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<List<RunnerStatus>> processes(ProjectReference projectReference) {
        List<DummyRunnerStatus> current = currentRunnerStatuses.get(projectReference.name());

        List<RunnerStatus> update = new ArrayList<>();
        if (current != null) {
            for (DummyRunnerStatus dummyRunnerStatus : current) {
                update.add(dummyRunnerStatus);
            }
        }


        return new DummyRequest<>(update);
    }


    public void registerRunnerStatus(ProjectReference projectReference, DummyRunnerStatus dummyRunnerStatus) {
        List<DummyRunnerStatus> list = availableRunnerStatuses.get(projectReference.name());
        if (list == null) {
            list = new ArrayList<>();
            availableRunnerStatuses.put(projectReference.name(), list);
        }
        list.add(dummyRunnerStatus);
    }
}
