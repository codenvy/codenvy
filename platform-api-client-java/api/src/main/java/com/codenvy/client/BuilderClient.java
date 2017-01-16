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


import com.codenvy.client.model.BuilderStatus;
import com.codenvy.client.model.ProjectReference;

import java.util.List;

/**
 * @author Florent Benoit
 */
public interface BuilderClient {
    /**
     * Builds the given {@link com.codenvy.client.model.ProjectReference} on codenvy.
     *
     * @param projectReference
     *         the project to build.
     * @return the {@link BuilderStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<BuilderStatus> build(ProjectReference projectReference);

    /**
     * Gets the status of the builder with the given task id.
     *
     * @param projectReference
     *         the project.
     * @param taskId
     *         the builder task id.
     * @return the {@link BuilderStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<BuilderStatus> status(ProjectReference projectReference, long taskId);

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
    Request<String> logs(ProjectReference projectReference, long taskId);

    /**
     * Cancels the builder with the given task id.
     *
     * @param projectReference
     *         the project.
     * @param taskId
     *         the builder task id.
     * @return the {@link BuilderStatus}.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<BuilderStatus> cancel(ProjectReference projectReference, long taskId);

    /**
     * Gets the project builds for the given project
     *
     * @param projectReference
     *         the project.
     * @return the different statuses.
     * @throws NullPointerException
     *         if project parameter is {@code null}.
     */
    Request<List<BuilderStatus>> builds(ProjectReference projectReference);
}
