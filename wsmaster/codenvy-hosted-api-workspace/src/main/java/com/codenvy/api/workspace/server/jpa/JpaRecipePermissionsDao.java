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
import com.codenvy.api.workspace.server.recipe.RecipePermissionsImpl;
import com.google.inject.persist.Transactional;


import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.event.BeforeRecipeRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Max Shaposhnik
 */
@Singleton
public class JpaRecipePermissionsDao extends AbstractJpaPermissionsDao<RecipePermissionsImpl> {

    private static final Logger LOG = LoggerFactory.getLogger(JpaRecipePermissionsDao.class);

    @Inject
    public JpaRecipePermissionsDao(AbstractPermissionsDomain<RecipePermissionsImpl> domain) throws IOException {
        super(domain);
    }

    @Override
    @Transactional
    public RecipePermissionsImpl get(String userId, String instanceId) throws ServerException, NotFoundException {
        requireNonNull(instanceId, "Stack identifier required");
        requireNonNull(userId, "User identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("RecipePermissions.getByUserAndRecipeId", RecipePermissionsImpl.class)
                                  .setParameter("recipeId", instanceId)
                                  .setParameter("userId", userId)
                                  .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException(format("Permissions on recipe '%s' of user '%s' was not found.", instanceId, userId));
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<RecipePermissionsImpl> getByInstance(String instanceId) throws ServerException {
        requireNonNull(instanceId, "Stack identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("RecipePermissions.getByRecipeId", RecipePermissionsImpl.class)
                                  .setParameter("recipeId", instanceId)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<RecipePermissionsImpl> getByUser(String userId) throws ServerException {
        requireNonNull(userId, "User identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("RecipePermissions.getByUserId", RecipePermissionsImpl.class)
                                  .setParameter("userId", userId)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }


    @Singleton
    public static class RemovePermissionsBeforeRecipeRemovedEventSubscriber implements EventSubscriber<BeforeRecipeRemovedEvent> {
        @Inject
        private EventService eventService;
        @Inject
        JpaRecipePermissionsDao dao;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this);
        }

        @Override
        public void onEvent(BeforeRecipeRemovedEvent event) {
            try {
                for (RecipePermissionsImpl permissions : dao.getByInstance(event.getRecipe().getId())) {
                    dao.remove(permissions.getUserId(), permissions.getInstanceId());
                }
            } catch (ServerException |NotFoundException x) {
                LOG.error(format("Couldn't remove permissions before recipe '%s' is removed", event.getRecipe().getId()), x);
            }
        }
    }
}
