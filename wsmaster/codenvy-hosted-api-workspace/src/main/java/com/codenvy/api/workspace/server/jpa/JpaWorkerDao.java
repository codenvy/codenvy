package com.codenvy.api.workspace.server.jpa;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.jpa.AbstractPermissionsDao;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.codenvy.api.workspace.server.spi.WorkerDao;

import org.eclipse.che.api.core.NotFoundException;
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
        super(new WorkspaceDomain(), WorkerImpl.class, "Worker.getByInstance");
    }

    @Override
    public WorkerImpl getWorker(String workspaceId, String userId) throws NotFoundException, ServerException {
        return super.get(userId, workspaceId);
    }

    @Override
    public void removeWorker(String workspaceId, String userId) throws ServerException, NotFoundException {
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
