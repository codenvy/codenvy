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
package com.codenvy.api.workspace.server.jpa;

import com.codenvy.api.permission.server.jpa.AbstractPermissionsDao;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.codenvy.api.workspace.server.spi.WorkerDao;

import org.eclipse.che.api.core.ServerException;

import java.io.IOException;
import java.util.List;

/**
 * @author Max Shaposhnik
 *
 */
public class JpaWorkerDao extends AbstractPermissionsDao<WorkerImpl> implements WorkerDao
{

    public JpaWorkerDao() throws IOException {
        super(new WorkspaceDomain(), WorkerImpl.class, "???");
    }

    @Override
    public WorkerImpl getWorker(String workspaceId, String userId) throws  ServerException {
        return super.get(userId, workspaceId);
    }

    @Override
    public void removeWorker(String workspaceId, String userId) throws ServerException {
        super.remove(userId, workspaceId);
    }

    @Override
    public List<WorkerImpl> getWorkers(String workspaceId) throws ServerException {
        return super.getByInstance(workspaceId);
    }

    @Override
    public List<WorkerImpl> getWorkersByUser(String userId) throws ServerException {
        return super.getByUser(userId);
    }


    @Override
    public List<WorkerImpl> getByUser(String userId) throws ServerException {
        return super.getByUser(userId);
    }
}
