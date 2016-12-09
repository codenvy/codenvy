/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
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
