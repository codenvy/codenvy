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
import com.codenvy.api.permission.server.jpa.AbstractJpaPermissionsDao;
import com.codenvy.api.workspace.server.stack.StackPermissionsImpl;
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
 * JPA based implementation of stack permissions DAO.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class JpaStackPermissionsDao extends AbstractJpaPermissionsDao<StackPermissionsImpl> {

    @Inject
    public JpaStackPermissionsDao(AbstractPermissionsDomain<StackPermissionsImpl> domain) throws IOException {
        super(domain);
    }

    @Override
    @Transactional
    public StackPermissionsImpl get(String userId, String instanceId) throws ServerException, NotFoundException {
        requireNonNull(instanceId, "Stack identifier required");
        requireNonNull(userId, "User identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("StackPermissions.getByUserAndStackId", StackPermissionsImpl.class)
                                  .setParameter("stackId", instanceId)
                                  .setParameter("userId", userId)
                                  .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException(format("Permissions on stack '%s' of user '%s' was not found.", instanceId, userId));
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<StackPermissionsImpl> getByInstance(String instanceId) throws ServerException {
        requireNonNull(instanceId, "Stack identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("StackPermissions.getByStackId", StackPermissionsImpl.class)
                                  .setParameter("stackId", instanceId)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<StackPermissionsImpl> getByUser(String userId) throws ServerException {
        requireNonNull(userId, "User identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("StackPermissions.getByUserId", StackPermissionsImpl.class)
                                  .setParameter("userId", userId)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }
}
