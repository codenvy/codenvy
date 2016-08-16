package com.codenvy.api.permission.server.dao;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.model.impl.PermissionsImpl;
import com.codenvy.api.permission.server.model.impl.PermissionsPrimaryKey;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.google.common.collect.ImmutableMap;
import com.google.inject.persist.Transactional;


import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author Max Shaposhnik
 *
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
            final PermissionsImpl result = managerProvider.get().find(???.class, new PermissionsPrimaryKey(userId, instanceId));
            if (result == null) {
                throw new NotFoundException(format("Permissions of user '%s' on domain %s with id '%s' was not found.", userId, domainId, instanceId));
            }
            return result;
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public List<PermissionsImpl> getByInstance(String domainId, String instanceId) throws ServerException, NotFoundException {
        return null;
    }

    @Override
    public boolean exists(String userId, String domainId, String instanceId, String action) throws ServerException {
        requireNonNull(userId, "User identifier required");
        requireNonNull(domainId, "Domain identifier required");
        requireNonNull(instanceId, "Instance identifier required");
        requireNonNull(action, "Action required");
        if (!idToDomain.get(domainId).getAllowedActions().contains(action)) {
            throw new ServerException(format("Specified domain '%s' cannot contain action '%s'", domainId, action));
        }
        try {
            final PermissionsImpl result = managerProvider.get().find(???.class, new PermissionsPrimaryKey(userId, instanceId));
            return result != null && result.getActions().contains(action);
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
    protected void doCreate(PermissionsImpl entity) {
        managerProvider.get().merge(entity);
    }

    @Transactional
    protected void doRemove(String userId, String domainId, String instanceId) throws NotFoundException {
        EntityManager manager = managerProvider.get();
        final PermissionsImpl entity = manager.find(???.class, new PermissionsPrimaryKey(userId, instanceId));
        if (entity == null) {
            throw new NotFoundException(format("Permissions of user '%s' on domain %s with id '%s' was not found.", userId, domainId, instanceId));
        }
        manager.remove(entity);
    }
}
