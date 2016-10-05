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
package com.codenvy.api.machine.server.jpa;

import com.codenvy.api.machine.server.recipe.RecipePermissionsImpl;
import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.jpa.AbstractJpaPermissionsDao;
import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.event.CascadeRemovalEventSubscriber;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.event.BeforeRecipeRemovedEvent;
import org.eclipse.che.commons.annotation.Nullable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * JPA based implementation of recipe permissions DAO.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class JpaRecipePermissionsDao extends AbstractJpaPermissionsDao<RecipePermissionsImpl> {

    @Inject
    public JpaRecipePermissionsDao(AbstractPermissionsDomain<RecipePermissionsImpl> domain) throws IOException {
        super(domain);
    }

    @Override
    public RecipePermissionsImpl get(String userId, String instanceId) throws ServerException, NotFoundException {
        requireNonNull(instanceId, "Recipe identifier required");
        requireNonNull(userId, "User identifier required");
        return doGet(wildcardToNull(userId), instanceId);
    }

    @Override
    public List<RecipePermissionsImpl> getByUser(String userId) throws ServerException {
        requireNonNull(userId, "User identifier required");
        return doGetByUser(wildcardToNull(userId));
    }

    @Override
    @Transactional
    public List<RecipePermissionsImpl> getByInstance(String instanceId) throws ServerException {
        requireNonNull(instanceId, "Recipe identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("RecipePermissions.getByRecipeId", RecipePermissionsImpl.class)
                                  .setParameter("recipeId", instanceId)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    protected List<RecipePermissionsImpl> doGetByUser(@Nullable String userId) throws ServerException {
        try {
            return managerProvider.get()
                                  .createNamedQuery("RecipePermissions.getByUserId", RecipePermissionsImpl.class)
                                  .setParameter("userId", userId)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    protected RecipePermissionsImpl doGet(@Nullable String userId, String instanceId) throws ServerException, NotFoundException {
        try {
            if (userId == null) {
                return managerProvider.get()
                                      .createNamedQuery("RecipePermissions.getByRecipeIdPublic", RecipePermissionsImpl.class)
                                      .setParameter("recipeId", instanceId)
                                      .getSingleResult();
            } else {
                return managerProvider.get()
                                      .createNamedQuery("RecipePermissions.getByUserAndRecipeId", RecipePermissionsImpl.class)
                                      .setParameter("recipeId", instanceId)
                                      .setParameter("userId", userId)
                                      .getSingleResult();
            }
        } catch (NoResultException e) {
            throw new NotFoundException(format("Permissions on recipe '%s' of user '%s' was not found.", instanceId, userId));
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }


    @Singleton
    public static class RemovePermissionsBeforeRecipeRemovedEventSubscriber
            extends CascadeRemovalEventSubscriber<BeforeRecipeRemovedEvent> {
        @Inject
        private EventService eventService;
        @Inject
        private JpaRecipePermissionsDao dao;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this, BeforeRecipeRemovedEvent.class);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this, BeforeRecipeRemovedEvent.class);
        }

        @Override
        public void onRemovalEvent(BeforeRecipeRemovedEvent event) throws Exception {
            for (RecipePermissionsImpl permissions : dao.getByInstance(event.getRecipe().getId())) {
                dao.remove(permissions.getUserId(), permissions.getInstanceId());
            }
        }
    }
}
