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

import com.codenvy.client.model.vfs.VfsInfo;

/**
 * Client API for dealing with VFS REST API.
 *
 * @author Florent Benoit
 */
public interface VFSClient {

    /**
     * Sets a VFS folder
     *
     * @param workspaceId
     *         the workspace ID.
     * @param path
     *         the VFS root folder.
     * @return a GIT URL that allow to clone the given project.
     */
    Request<VfsInfo> directoryMapping(String workspaceId, String path);


}
