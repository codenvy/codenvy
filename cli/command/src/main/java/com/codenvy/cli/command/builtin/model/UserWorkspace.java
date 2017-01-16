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

import com.codenvy.cli.command.builtin.MultiRemoteCodenvy;
import com.codenvy.client.Codenvy;

import java.util.List;

/**
 * Represents the workspace and its associated projects
 *
 * @author Florent Benoit
 */
public interface UserWorkspace {

    /**
     * @return identifier of the workspace
     */
    String id();

    /**
     * @return the name of the workspace
     */
    String name();

    /**
     * @return projects that are in this workspace
     */
    List<UserProjectReference> getProjects();

    /**
     * @return remote of this workspace
     */
    String getRemote();

    MultiRemoteCodenvy getMultiRemoteCodenvy();

    Codenvy getCodenvy();

}
