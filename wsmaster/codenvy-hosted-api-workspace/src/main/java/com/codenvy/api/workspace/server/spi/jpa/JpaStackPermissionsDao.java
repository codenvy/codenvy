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
package com.codenvy.api.workspace.server.spi.jpa;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.jpa.AbstractJpaPermissionsDao;
import com.codenvy.api.workspace.server.stack.StackPermissionsImpl;
import com.google.api.client.repackaged.com.google.common.annotations.VisibleForTesting;
import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.event.BeforeStackRemovedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.core.db.event.CascadeRemovalEventSubscriber;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * JPA based implementation of stack permissions DAO.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class JpaStackPermissionsDao extends AbstractJpaPermissionsDao<StackPermissionsImpl> {

    @Inject
    public JpaStackPermissionsDao(AbstractPermissionsDomain<StackPermissionsImpl> domain) {
        super(domain);
    }

    @Override
    public StackPermissionsImpl get(String userId, String instanceId) throws ServerException, NotFoundException {
        requireNonNull(instanceId, "Stack identifier required");
        requireNonNull(userId, "User identifier required");
        try {
            return new StackPermissionsImpl(getEntity(wildcardToNull(userId), instanceId));
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    public List<StackPermissionsImpl> getByUser(String userId) throws ServerException {
        requireNonNull(userId, "User identifier required");
        return doGetByUser(wildcardToNull(userId)).stream()
                                                  .map(StackPermissionsImpl::new)
                                                  .collect(toList());
    }

    @Override
    @Transactional
    public Page<StackPermissionsImpl> getByInstance(String instanceId, int maxItems, long skipCount) throws ServerException {
        requireNonNull(instanceId, "Stack identifier required");
        checkArgument(skipCount <= Integer.MAX_VALUE, "The number of items to skip can't be greater than " + Integer.MAX_VALUE);

        try {
            final EntityManager entityManager = managerProvider.get();
            final List<StackPermissionsImpl> stacks = entityManager.createNamedQuery("StackPermissions.getByStackId",
                                                                                     StackPermissionsImpl.class)
                                                                   .setFirstResult((int)skipCount)
                                                                   .setMaxResults(maxItems)
                                                                   .setParameter("stackId", instanceId)
                                                                   .getResultList()
                                                                   .stream()
                                                                   .map(StackPermissionsImpl::new)
                                                                   .collect(toList());
            final Long permissionsCount = entityManager.createNamedQuery("StackPermissions.getCountByStackId", Long.class)
                                                       .setParameter("stackId", instanceId)
                                                       .getSingleResult();

            return new Page<>(stacks, skipCount, maxItems, permissionsCount);
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected StackPermissionsImpl getEntity(String userId, String instanceId) throws NotFoundException {
        try {
            return doGet(userId, instanceId);
        } catch (NoResultException e) {
            throw new NotFoundException(format("Permissions on stack '%s' of user '%s' was not found.", instanceId, userId));
        }
    }

    @Transactional
    protected StackPermissionsImpl doGet(String userId, String instanceId) {
        if (userId == null) {
            return managerProvider.get()
                                  .createNamedQuery("StackPermissions.getByStackIdPublic", StackPermissionsImpl.class)
                                  .setParameter("stackId", instanceId)
                                  .getSingleResult();
        } else {
            return managerProvider.get()
                                  .createNamedQuery("StackPermissions.getByUserAndStackId", StackPermissionsImpl.class)
                                  .setParameter("stackId", instanceId)
                                  .setParameter("userId", userId)
                                  .getSingleResult();
        }
    }

    @Transactional
    protected List<StackPermissionsImpl> doGetByUser(@Nullable String userId) throws ServerException {
        try {
            return managerProvider.get()
                                  .createNamedQuery("StackPermissions.getByUserId", StackPermissionsImpl.class)
                                  .setParameter("userId", userId)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Singleton
    public static class RemovePermissionsBeforeStackRemovedEventSubscriber
            extends CascadeRemovalEventSubscriber<BeforeStackRemovedEvent> {
        private static final int PAGE_SIZE = 100;
        @Inject
        private EventService           eventService;
        @Inject
        private JpaStackPermissionsDao dao;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this, BeforeStackRemovedEvent.class);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this, BeforeStackRemovedEvent.class);
        }

        @Override
        public void onRemovalEvent(BeforeStackRemovedEvent event) throws Exception {
            removeStackPermissions(event.getStack().getId(), PAGE_SIZE);
        }

        @VisibleForTesting
        void removeStackPermissions(String stackId, int pageSize) throws ServerException, NotFoundException {
            Page<StackPermissionsImpl> stacksPage;
            do {
                // skip count always equals to 0 because elements will be shifted after removing previous items
                stacksPage = dao.getByInstance(stackId, pageSize, 0);
                for (StackPermissionsImpl stackPermissions : stacksPage.getItems()) {
                    dao.remove(stackPermissions.getUserId(), stackPermissions.getInstanceId());
                }
            } while (stacksPage.hasNextPage());

        }
    }
}
