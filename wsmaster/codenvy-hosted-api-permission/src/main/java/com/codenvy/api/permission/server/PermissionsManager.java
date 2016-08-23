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
package com.codenvy.api.permission.server;

import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.permission.shared.model.Permissions;
import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.codenvy.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;

/**
 * Facade for Permissions related operations.
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
@Singleton
public class PermissionsManager {
    private final Map<String, PermissionsDao>            domainToDao;
    private final Map<String, AbstractPermissionsDomain> domains;

    @Inject
    public PermissionsManager(Set<PermissionsDao> storages) throws ServerException {
        Map<String, PermissionsDao> domainToDao = new HashMap<>();
        Map<String, AbstractPermissionsDomain> domains = new HashMap<>();
        for (PermissionsDao storage : storages) {
            AbstractPermissionsDomain domain = storage.getDomain();
            domains.put(domain.getId(), domain);
            PermissionsDao oldStorage = domainToDao.put(domain.getId(), storage);
            if (oldStorage != null) {
                throw new ServerException("Permissions Domain '" + domain.getId() + "' should be stored in only one storage. " +
                                          "Duplicated in " + storage.getClass() + " and " + oldStorage.getClass());
            }
        }
        this.domainToDao = ImmutableMap.copyOf(domainToDao);
        this.domains = ImmutableMap.copyOf(domains);
    }

    /**
     * Stores (adds or updates) permissions.
     *
     * @param permissions
     *         permission to store
     * @throws NotFoundException
     *         when permissions have unsupported domain
     * @throws ConflictException
     *         when new permissions remove last 'setPermissions' of given instance
     * @throws ServerException
     *         when any other error occurs during permissions storing
     */
    public void storePermission(Permissions permissions) throws ServerException, ConflictException, NotFoundException {
        final String domainId = permissions.getDomainId();
        final String instanceId = permissions.getInstanceId();
        final String userId = permissions.getUserId();

        final PermissionsDao permissionsStorage = getPermissionsDao(domainId);
        if (!permissions.getActions().contains(SET_PERMISSIONS)
            && userHasLastSetPermissions(permissionsStorage, userId, instanceId)) {
            throw new ConflictException("Can't edit permissions because there is not any another user with permission 'setPermissions'");
        }

        final AbstractPermissionsDomain permissionsDomain = getDomain(permissions.getDomainId());
        final AbstractPermissions permissionsEntity = permissionsDomain.newInstance(userId, instanceId, permissions.getActions());

        final Set<String> allowedActions = new HashSet<>(permissionsDomain.getAllowedActions());
        final Set<String> unsupportedActions = permissionsEntity.getActions()
                                                                .stream()
                                                                .filter(action -> !allowedActions.contains(action))
                                                                .collect(Collectors.toSet());
        if (!unsupportedActions.isEmpty()) {
            throw new ConflictException("Domain with id '" + permissions.getDomainId() + "' doesn't support next action(s): " +
                                        unsupportedActions.stream()
                                                          .collect(Collectors.joining(", ")));
        }

        permissionsStorage.store(permissionsEntity);
    }

    /**
     * @param userId
     *         user id
     * @param domainId
     *         domain id
     * @param instanceId
     *         instance id
     * @return userId's permissions for specified instanceId
     * @throws NotFoundException
     *         when given domainId is unsupported
     * @throws NotFoundException
     *         when permissions with given userId and domainId and instanceId was not found
     * @throws ServerException
     *         when any other error occurs during permissions fetching
     */
    public AbstractPermissions get(String userId, String domainId, String instanceId)
            throws ServerException, NotFoundException, ConflictException {
        return getPermissionsDao(domainId).get(userId, instanceId);
    }

    /**
     * @param domainId
     *         domain id
     * @param instanceId
     *         instance id
     * @return set of permissions
     * @throws NotFoundException
     *         when given domainId is unsupported
     * @throws ServerException
     *         when any other error occurs during permissions fetching
     */
    public List<AbstractPermissions> getByInstance(String domainId, String instanceId)
            throws ServerException, NotFoundException, ConflictException {
        return getPermissionsDao(domainId).getByInstance(instanceId);
    }

    /**
     * Removes permissions of userId related to the particular instanceId of specified domainId
     *
     * @param userId
     *         user id
     * @param domainId
     *         domain id
     * @param instanceId
     *         instance id
     * @throws NotFoundException
     *         when given domainId is unsupported
     * @throws ConflictException
     *         when removes last 'setPermissions' of given instanceId
     * @throws ServerException
     *         when any other error occurs during permissions removing
     */
    public void remove(String userId, String domainId, String instanceId) throws ConflictException, ServerException, NotFoundException {
        final PermissionsDao permissionsStorage = getPermissionsDao(domainId);
        if (userHasLastSetPermissions(permissionsStorage, userId, instanceId)) {
            throw new ConflictException("Can't remove permissions because there is not any another user with permission 'setPermissions'");
        }
        permissionsStorage.remove(userId, instanceId);
    }

    /**
     * @param userId
     *         user id
     * @param domainId
     *         domain id
     * @param instanceId
     *         instance id
     * @param action
     *         action name
     * @return true if the permission exists
     * @throws NotFoundException
     *         when given domainId is unsupported
     * @throws ServerException
     *         when any other error occurs during permission existence checking
     */
    public boolean exists(String userId, String domainId, String instanceId, String action) throws ServerException,
                                                                                                   NotFoundException,
                                                                                                   ConflictException {
        return getDomain(domainId).getAllowedActions().contains(action)
               && getPermissionsDao(domainId).exists(userId, instanceId, action);
    }

    /**
     * Returns supported domains
     */
    public List<AbstractPermissionsDomain> getDomains() {
        return new ArrayList<>(domains.values());
    }

    /**
     * Returns supported domain
     *
     * @throws NotFoundException
     *         when given domain is unsupported
     */
    public AbstractPermissionsDomain getDomain(String domain) throws NotFoundException {
        final AbstractPermissionsDomain permissionsDomain = domains.get(domain);
        if (permissionsDomain == null) {
            throw new NotFoundException("Requested unsupported domain '" + domain + "'");
        }
        return domains.get(domain);
    }


    private PermissionsDao getPermissionsDao(String domain) throws NotFoundException {
        final PermissionsDao permissionsStorage = domainToDao.get(domain);
        if (permissionsStorage == null) {
            throw new NotFoundException("Requested unsupported domain '" + domain + "'");
        }
        return permissionsStorage;
    }

    private boolean userHasLastSetPermissions(PermissionsDao<AbstractPermissions> permissionsStorage, String userId, String instanceId)
            throws ServerException, ConflictException {
        try {
            return permissionsStorage.exists(userId, instanceId, SET_PERMISSIONS)
                   && !permissionsStorage.getByInstance(instanceId)
                                         .stream()
                                         .anyMatch(permission -> !permission.getUserId().equals(userId)
                                                                 && permission.getActions().contains(SET_PERMISSIONS));
        } catch (NotFoundException e) {
            return true;
        }
    }
}
