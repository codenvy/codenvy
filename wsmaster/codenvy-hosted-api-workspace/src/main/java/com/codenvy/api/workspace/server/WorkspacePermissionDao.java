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
package com.codenvy.api.workspace.server;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.model.impl.PermissionsImpl;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.permission.shared.model.Permissions;
import com.codenvy.api.workspace.server.spi.WorkerDao;
import com.codenvy.api.workspace.server.model.Worker;
import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link PermissionsDao} for storing permissions of {@link WorkspaceDomain}
 *
 * <p>This implementation adapts {@link Permissions} and {@link Worker} and use
 * {@link WorkerDao} as storage of permissions
 *
 * @author Sergii Leschenko
 */
@Singleton
public class WorkspacePermissionDao implements PermissionsDao {
    private final Set<AbstractPermissionsDomain> supportedDomain;
    private final WorkerDao                      workerDao;

    @Inject
    public WorkspacePermissionDao(WorkerDao workerDao) {
        this.workerDao = workerDao;
        this.supportedDomain = ImmutableSet.of(new WorkspaceDomain());
    }

    @Override
    public Set<AbstractPermissionsDomain> getDomains() {
        return supportedDomain;
    }

    @Override
    public void store(PermissionsImpl permissions) throws ServerException {
        workerDao.store(new WorkerImpl(permissions.getInstanceId(),
                                       permissions.getUserId(),
                                       permissions.getActions()));
    }

    @Override
    public PermissionsImpl get(String user, String domain, String instance) throws ServerException, NotFoundException {
        return toPermission(workerDao.getWorker(instance, user));
    }

    @Override
    public List<PermissionsImpl> getByInstance(String domain, String instance) throws ServerException {
        return toPermissions(workerDao.getWorkers(instance));
    }

    @Override
    public boolean exists(String user, String domain, String instance, String action) throws ServerException {
        try {
            return workerDao.getWorker(instance, user).getActions()
                            .stream()
                            .anyMatch(action::equals);
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public void remove(String user, String domain, String instance) throws ServerException, NotFoundException {
        workerDao.removeWorker(instance, user);
    }

    private PermissionsImpl toPermission(WorkerImpl worker) {
        return new PermissionsImpl(worker.getUserId(),
                                   WorkspaceDomain.DOMAIN_ID,
                                   worker.getWorkspaceId(),
                                   worker.getActions());
    }

    private List<PermissionsImpl> toPermissions(List<WorkerImpl> workers) {
        return workers.stream()
                      .map(this::toPermission)
                      .collect(Collectors.toList());
    }
}
