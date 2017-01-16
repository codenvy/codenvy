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
package com.codenvy.client.dummy.builder;

import com.codenvy.client.BuilderClient;
import com.codenvy.client.Request;
import com.codenvy.client.dummy.DummyRequest;
import com.codenvy.client.model.BuilderState;
import com.codenvy.client.model.BuilderStatus;
import com.codenvy.client.model.ProjectReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Florent Benoit
 */
public class DummyBuilderClient implements BuilderClient {

    /**
     * List of available builder status per project name.
     */
    private Map<String, List<DummyBuilderStatus>> availableBuilderStatuses;

    /**
     * List of available builder status per project name.
     */
    private Map<String, List<DummyBuilderStatus>> currentBuilderStatuses;

    /**
     * Current builder index
     */
    private int builderStatusIndex = 0;



    public DummyBuilderClient() {
        this.availableBuilderStatuses = new HashMap<>();
        this.currentBuilderStatuses = new HashMap<>();
    }


    /**
     * Builds the given {@link com.codenvy.client.model.ProjectReference} on codenvy.
     *
     * @param projectReference
     *         the project to build.
     * @return the {@link com.codenvy.client.model.BuilderStatus}.
     */
    @Override
    public Request<BuilderStatus> build(final ProjectReference projectReference) {
       String projectName = projectReference.name();
        List<DummyBuilderStatus> statuses = availableBuilderStatuses.get(projectName);
        DummyBuilderStatus builderStatus = null;
        if (statuses == null || statuses.isEmpty()) {
            builderStatus = null;
        } else {
            builderStatus = statuses.get(builderStatusIndex);
            // add it to current process
            builderStatus.setStatus(BuilderState.IN_QUEUE);

            List<DummyBuilderStatus> currents = currentBuilderStatuses.get(projectName);
            if (currents == null) {
                currents = new ArrayList<>();
                currentBuilderStatuses.put(projectName, currents);
            }

            currents.add(builderStatus);
        }
        return new DummyRequest<BuilderStatus>(builderStatus);
    }

    /**
     * Gets the status of the builder with the given task id.
     *
     * @param projectReference
     *         the project.
     * @param taskId
     *         the builder task id.
     * @return the {@link com.codenvy.client.model.BuilderStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<BuilderStatus> status(ProjectReference projectReference, long taskId) {

        List<DummyBuilderStatus> statuses = availableBuilderStatuses.get(projectReference.name());
        if (statuses != null && !statuses.isEmpty()) {
            // search matching status
            for (BuilderStatus builderStatus : statuses) {
                if (taskId == builderStatus.taskId()) {
                    return new DummyRequest<>(builderStatus);
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
        List<DummyBuilderStatus> statuses = availableBuilderStatuses.get(projectReference.name());
        if (statuses != null && !statuses.isEmpty()) {
            // search matching status
            for (DummyBuilderStatus builderStatus : statuses) {
                if (taskId == builderStatus.taskId()) {
                    return new DummyRequest<>(builderStatus.getLog());
                }
            }
        }
        // not found
        return new DummyRequest<>(null);
    }

    /**
     * Cancels the builder with the given task id.
     *
     * @param projectReference
     *         the project.
     * @param taskId
     *         the builder task id.
     * @return the {@link com.codenvy.client.model.BuilderStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<BuilderStatus> cancel(ProjectReference projectReference, long taskId) {
        List<DummyBuilderStatus> statuses = availableBuilderStatuses.get(projectReference.name());
        DummyBuilderStatus found = null;
        if (statuses != null && !statuses.isEmpty()) {
            // search matching status
            for (DummyBuilderStatus builderStatus : statuses) {
                if (taskId == builderStatus.taskId()) {
                    found = builderStatus;
                }
            }
        }
        // cancel
        if (found != null) {
            found.setStatus(BuilderState.CANCELLED);
            // remove if found
            List<DummyBuilderStatus> currents = currentBuilderStatuses.get(projectReference.name());
            if (currents != null) {
                currents.remove(found);
            }
        }


        return new DummyRequest<BuilderStatus>(found);
    }

    /**
     * Gets the project builds for the given project
     *
     * @param projectReference
     *         the project.
     * @return the different statuses.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    @Override
    public Request<List<BuilderStatus>> builds(ProjectReference projectReference) {
        List<DummyBuilderStatus> current = currentBuilderStatuses.get(projectReference.name());

        List<BuilderStatus> update = new ArrayList<>();
        if (current != null) {
            for (DummyBuilderStatus dummyBuilderStatus : current) {
                update.add(dummyBuilderStatus);
            }
        }


        return new DummyRequest<>(update);
    }


    public void registerBuilderStatus(ProjectReference projectReference, DummyBuilderStatus dummyBuilderStatus) {
        List<DummyBuilderStatus> list = availableBuilderStatuses.get(projectReference.name());
        if (list == null) {
            list = new ArrayList<>();
            availableBuilderStatuses.put(projectReference.name(), list);
        }
        list.add(dummyBuilderStatus);

    }
}
