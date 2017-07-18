/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.machine.backup;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

/**
 * Copies workspace files between machine and backup storage.
 *
 * @author Alexander Garagatyi
 */
public interface EnvironmentBackupManager {
    /**
     * Copy files of workspace into backup storage.
     *
     * @param workspaceId
     *         id of workspace to backup
     * @throws NotFoundException
     *         if workspace is not found or not running
     * @throws ServerException
     *         if any other error occurs
     */
    void backupWorkspace(String workspaceId) throws ServerException, NotFoundException;
}
