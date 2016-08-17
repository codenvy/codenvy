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
package com.codenvy.api.permission.server.jpa;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.dao.CommonDomains;
import com.codenvy.api.permission.server.model.impl.PermissionsImpl;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.google.common.collect.ImmutableMap;
import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author Max Shaposhnik
 */
@Singleton
public class JpaCommonPermissionsDao implements PermissionsDao {

    private final Map<String, AbstractPermissionsDomain> idToDomain;

    @Inject
    private Provider<EntityManager> managerProvider;

    @Inject
    public JpaCommonPermissionsDao(@CommonDomains Set<AbstractPermissionsDomain> permissionsDomains) throws IOException {
        final ImmutableMap.Builder<String, AbstractPermissionsDomain> mapBuilder = ImmutableMap.builder();
        permissionsDomains.stream()
                          .forEach(domain -> mapBuilder.put(domain.getId(), domain));
        idToDomain = mapBuilder.build();
    }

    @Override
    public Set<AbstractPermissionsDomain> getDomains() {
        return new HashSet<>(idToDomain.values());
    }

    @Override
    public void store(PermissionsImpl permissions) throws ServerException, NotFoundException {
        requireNonNull(permissions, "Permissions required");
        try {
            doCreate(permissions);
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Override
    @Transactional
    public PermissionsImpl get(String userId, String domainId, String instanceId) throws ServerException, NotFoundException {
        requireNonNull(userId, "User identifier required");
        requireNonNull(domainId, "Domain identifier required");
        requireNonNull(instanceId, "Instance identifier required");
        try {
            final PermissionsImpl result = managerProvider.get()
                                                          .createNamedQuery("Permissions.getByUserDomainAndInstance", PermissionsImpl.class)
                                                          .setParameter("instanceId", instanceId)
                                                          .setParameter("userId", userId)
                                                          .setParameter("domainId", domainId)
                                                          .getSingleResult();
            return result;
        } catch (NoResultException n) {
            throw new NotFoundException(
                    format("Permissions of user '%s' on domain '%s' with id '%s' was not found.", userId, domainId, instanceId));
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<PermissionsImpl> getByInstance(String domainId, String instanceId) throws ServerException, NotFoundException {
        requireNonNull(domainId, "Domain identifier required");
        requireNonNull(instanceId, "Instance identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("Permissions.getByDomainAndInstance", PermissionsImpl.class)
                                  .setParameter("instanceId", instanceId)
                                  .setParameter("domainId", domainId)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean exists(String userId, String domainId, String instanceId, String action) throws ServerException {
        requireNonNull(userId, "User identifier required");
        requireNonNull(domainId, "Domain identifier required");
        requireNonNull(instanceId, "Instance identifier required");
        requireNonNull(action, "Action required");
        try {
            final PermissionsImpl result = managerProvider.get()
                                                          .createNamedQuery("Permissions.getByUserDomainAndInstance", PermissionsImpl.class)
                                                          .setParameter("instanceId", instanceId)
                                                          .setParameter("userId", userId)
                                                          .setParameter("domainId", domainId)
                                                          .getSingleResult();
            return result.getActions().contains(action);
        } catch (NoResultException n) {
            return false;
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void remove(String userId, String domainId, String instanceId) throws ServerException, NotFoundException {
        requireNonNull(userId, "User identifier required");
        requireNonNull(domainId, "Domain identifier required");
        requireNonNull(instanceId, "Instance identifier required");
        try {
            doRemove(userId, domainId, instanceId);
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }

    }

    @Transactional
    protected void doCreate(PermissionsImpl entity) throws ServerException {
        EntityManager manager = managerProvider.get();
        try {
            final PermissionsImpl result = manager.createNamedQuery("Permissions.getByUserDomainAndInstance", PermissionsImpl.class)
                                                  .setParameter("instanceId", entity.getInstanceId())
                                                  .setParameter("userId", entity.getUserId())
                                                  .setParameter("domainId", entity.getDomainId())
                                                  .getSingleResult();
            result.getActions().clear();
            result.getActions().addAll(entity.getActions());
        } catch (NoResultException n) {
            manager.persist(entity);
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    protected void doRemove(String userId, String domainId, String instanceId) throws NotFoundException {
        EntityManager manager = managerProvider.get();
        PermissionsImpl entity;
        try {
            entity = manager.createNamedQuery("Permissions.getByUserDomainAndInstance", PermissionsImpl.class)
                            .setParameter("instanceId", instanceId)
                            .setParameter("userId", userId)
                            .setParameter("domainId", domainId)
                            .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException(
                    format("Permissions of user '%s' on domain '%s' with id '%s' was not found.", userId, domainId, instanceId));
        }
        manager.remove(entity);
    }
}
