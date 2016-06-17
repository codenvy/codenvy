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
package com.codenvy.api.workspace.server.recipe;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.PermissionsImpl;
import com.codenvy.api.permission.server.dao.PermissionsStorage;
import com.codenvy.api.permission.shared.Permissions;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.acl.AclEntry;
import org.eclipse.che.api.core.acl.AclEntryImpl;
import org.eclipse.che.api.machine.server.dao.RecipeDao;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link PermissionsStorage} for storing permissions of {@link RecipeDomain}
 *
 * <p>This implementation adapts {@link Permissions} and {@link AclEntry} and use
 * {@link RecipeDao} as storage of permissions
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RecipePermissionStorage implements PermissionsStorage {
    private final Set<AbstractPermissionsDomain> supportedDomain;
    private final RecipeDao                      recipeDao;

    @Inject
    public RecipePermissionStorage(RecipeDao recipeDao) {
        this.recipeDao = recipeDao;
        supportedDomain = ImmutableSet.of(new RecipeDomain());
    }

    @Override
    public Set<AbstractPermissionsDomain> getDomains() {
        return supportedDomain;
    }

    @Override
    public void store(PermissionsImpl permissions) throws ServerException, NotFoundException {
        final RecipeImpl recipe = recipeDao.getById(permissions.getInstance());
        recipe.getAcl().removeIf(aclEntry -> aclEntry.getUser().equals(permissions.getUser()));
        recipe.getAcl().add(new AclEntryImpl(permissions.getUser(),
                                             permissions.getActions()));
        recipeDao.update(recipe);
    }

    @Override
    public PermissionsImpl get(String user, String domain, String instance) throws ServerException, NotFoundException {
        final RecipeImpl recipe = recipeDao.getById(instance);
        final Optional<AclEntryImpl> result = recipe.getAcl()
                                                    .stream()
                                                    .filter(aclEntry -> aclEntry.getUser().equals(user))
                                                    .findAny();

        if (!result.isPresent()) {
            throw new NotFoundException(String.format("Permissions for user '%s' and instance '%s' of domain '%s' was not found",
                                                      user, instance, domain));
        }

        return toPermission(instance, result.get());
    }

    @Override
    public List<PermissionsImpl> getByInstance(String domain, String instance) throws ServerException, NotFoundException {
        final RecipeImpl recipe = recipeDao.getById(instance);
        return toPermissions(instance, recipe.getAcl());
    }

    @Override
    public boolean exists(String user, String domain, String instance, String action) throws ServerException {
        RecipeImpl recipe;
        try {
            recipe = recipeDao.getById(instance);
        } catch (NotFoundException e) {
            return false;
        }

        return recipe.getAcl() != null
               && recipe.getAcl()
                        .stream()
                        .filter(entry -> entry.getUser().equals(user) || entry.getUser().equals("*"))
                        .flatMap(entry -> entry.getActions().stream())
                        .anyMatch(action::equals);
    }

    @Override
    public void remove(String user, String domain, String instance) throws ServerException, NotFoundException {
        final RecipeImpl recipe = recipeDao.getById(instance);
        recipe.getAcl().removeIf(aclEntry -> aclEntry.getUser().equals(user));
        recipeDao.update(recipe);
    }

    private List<PermissionsImpl> toPermissions(String stack, List<AclEntryImpl> acls) {
        return acls.stream()
                   .map(acl -> toPermission(stack, acl))
                   .collect(Collectors.toList());
    }

    private PermissionsImpl toPermission(String stack, AclEntryImpl acl) {
        return new PermissionsImpl(acl.getUser(),
                                   RecipeDomain.DOMAIN_ID,
                                   stack,
                                   acl.getActions());
    }
}
