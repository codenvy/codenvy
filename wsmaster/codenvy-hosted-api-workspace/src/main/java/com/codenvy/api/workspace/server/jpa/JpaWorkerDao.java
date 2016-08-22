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

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.jpa.AbstractPermissionsDao;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.codenvy.api.workspace.server.spi.WorkerDao;
import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author Max Shaposhnik
 *
 */
@Singleton
public class JpaWorkerDao extends AbstractPermissionsDao<WorkerImpl> implements WorkerDao
{
    @Inject
    public JpaWorkerDao(AbstractPermissionsDomain<WorkerImpl> supportedDomain) throws IOException {
        super(supportedDomain, WorkerImpl.class);
    }

    @Override
    public WorkerImpl getWorker(String workspaceId, String userId) throws  ServerException, NotFoundException {
        return get(userId, workspaceId);
    }

    @Override
    public void removeWorker(String workspaceId, String userId) throws ServerException {
        try {
            super.remove(userId, workspaceId);
        } catch (NotFoundException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public List<WorkerImpl> getWorkers(String workspaceId) throws ServerException {
        return getByInstance(workspaceId);
    }

    @Override
    public List<WorkerImpl> getWorkersByUser(String userId) throws ServerException {
        return getByUser(userId);
    }


    @Override
    @Transactional
    public WorkerImpl get(String userId, String instanceId) throws ServerException, NotFoundException {
        requireNonNull(instanceId, "Workspace identifier required");
        requireNonNull(userId, "User identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("Worker.getByUserAndWorkspaceId", WorkerImpl.class)
                                  .setParameter("workspaceId", instanceId)
                                  .setParameter("userId", userId)
                                  .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException(format("Worker of workspace '%s' with id '%s' was not found.", instanceId, userId));
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<WorkerImpl> getByInstance(String instanceId) throws ServerException {
        requireNonNull(instanceId, "Workspace identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("Worker.getByWorkspaceId", WorkerImpl.class)
                                  .setParameter("workspaceId", instanceId)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<WorkerImpl> getByUser(String userId) throws ServerException {
        requireNonNull(userId, "User identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("Worker.getByUserId", WorkerImpl.class)
                                  .setParameter("userId", userId)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }
}
