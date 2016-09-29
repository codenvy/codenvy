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
package com.codenvy.resource.spi.jpa;

import com.codenvy.resource.spi.FreeResourcesLimitDao;
import com.codenvy.resource.spi.impl.FreeResourcesLimitImpl;
import com.google.inject.persist.Transactional;

import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * JPA based implementation of {@link FreeResourcesLimitDao}.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class JpaFreeResourcesLimitDao implements FreeResourcesLimitDao {
    private static final Logger LOG = LoggerFactory.getLogger(JpaFreeResourcesLimitDao.class);

    @Inject
    private Provider<EntityManager> managerProvider;

    @Override
    public void store(FreeResourcesLimitImpl resourcesLimit) throws ServerException {
        requireNonNull(resourcesLimit, "Required non-null resource limit");
        try {
            doStore(resourcesLimit);
        } catch (RuntimeException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public FreeResourcesLimitImpl get(String accountId) throws NotFoundException, ServerException {
        requireNonNull(accountId, "Required non-null account id");
        try {
            return managerProvider.get()
                                  .createNamedQuery("FreeResourcesLimit.get", FreeResourcesLimitImpl.class)
                                  .setParameter("accountId", accountId)
                                  .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("Free resources limit for account '" + accountId + "' was not found");
        } catch (RuntimeException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Page<FreeResourcesLimitImpl> getAll(int maxItems, int skipCount) throws ServerException {
        try {
            final List<FreeResourcesLimitImpl> list = managerProvider.get()
                                                                     .createNamedQuery("FreeResourcesLimit.getAll",
                                                                                       FreeResourcesLimitImpl.class)
                                                                     .setMaxResults(maxItems)
                                                                     .setFirstResult(skipCount)
                                                                     .getResultList();
            return new Page<>(list, skipCount, maxItems, getTotalCount());
        } catch (RuntimeException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public void remove(String id) throws ServerException {
        requireNonNull(id, "Required non-null id");
        try {
            doRemove(id);
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Transactional
    protected void doRemove(String id) {
        final EntityManager manager = managerProvider.get();
        final FreeResourcesLimitImpl resourcesLimit = manager.find(FreeResourcesLimitImpl.class, id);
        if (resourcesLimit != null) {
            manager.remove(resourcesLimit);
        }
    }

    @Transactional
    protected void doStore(FreeResourcesLimitImpl resourcesLimit) throws ServerException {
        EntityManager manager = managerProvider.get();
        try {
            final FreeResourcesLimitImpl existedLimit = get(resourcesLimit.getAccountId());
            existedLimit.setResources(resourcesLimit.getResources());
        } catch (NotFoundException n) {
            manager.persist(resourcesLimit);
        }
    }

    private long getTotalCount() throws ServerException {
        return managerProvider.get()
                              .createNamedQuery("FreeResourcesLimit.getTotalCount", Long.class)
                              .getSingleResult();
    }

    @Singleton
    public static class RemoveFreeResourcesLimitBeforeAccountRemovedEventSubscriber implements EventSubscriber<BeforeAccountRemovedEvent> {
        @Inject
        private EventService eventService;
        @Inject
        private FreeResourcesLimitDao limitDao;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this);
        }

        @Override
        public void onEvent(BeforeAccountRemovedEvent event) {
            try {
                limitDao.remove(event.getAccount().getId());
            } catch (Exception x) {
                LOG.error(format("Couldn't remove workspaces before account '%s' is removed", event.getAccount().getId()), x);
            }
        }
    }
}
