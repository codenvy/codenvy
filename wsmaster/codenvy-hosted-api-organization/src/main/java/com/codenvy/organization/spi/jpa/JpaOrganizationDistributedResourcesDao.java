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
package com.codenvy.organization.spi.jpa;

import com.codenvy.organization.api.event.BeforeOrganizationRemovedEvent;
import com.codenvy.organization.spi.OrganizationDistributedResourcesDao;
import com.codenvy.organization.spi.impl.OrganizationDistributedResourcesImpl;
import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * JPA based implementation of {@link OrganizationDistributedResourcesDao}.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class JpaOrganizationDistributedResourcesDao implements OrganizationDistributedResourcesDao {
    private static final Logger LOG = LoggerFactory.getLogger(JpaOrganizationDistributedResourcesDao.class);

    @Inject
    private Provider<EntityManager> managerProvider;

    @Override
    public void store(OrganizationDistributedResourcesImpl distributedResources) throws ServerException {
        requireNonNull(distributedResources, "Required non-null distributed resources");
        try {
            doStore(distributedResources);
        } catch (RuntimeException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public OrganizationDistributedResourcesImpl get(String organizationId) throws NotFoundException, ServerException {
        requireNonNull(organizationId, "Required non-null organization id");
        try {
            OrganizationDistributedResourcesImpl distributedResources = managerProvider.get()
                                                                                       .find(OrganizationDistributedResourcesImpl.class,
                                                                                             organizationId);
            if (distributedResources == null) {
                throw new NotFoundException("There are no distributed resources for organization with id '" + organizationId + "'.");
            }

            return new OrganizationDistributedResourcesImpl(distributedResources);
        } catch (RuntimeException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Page<OrganizationDistributedResourcesImpl> getByParent(String organizationId, int maxItems, long skipCount)
            throws ServerException {
        requireNonNull(organizationId, "Required non-null organization id");
        checkArgument(skipCount <= Integer.MAX_VALUE, "The number of items to skip can't be greater than " + Integer.MAX_VALUE);
        try {
            final EntityManager manager = managerProvider.get();
            final List<OrganizationDistributedResourcesImpl> distributedResources =
                    manager.createNamedQuery("OrganizationDistributedResources.getByParent",
                                             OrganizationDistributedResourcesImpl.class)
                           .setParameter("parent", organizationId)
                           .setMaxResults(maxItems)
                           .setFirstResult((int)skipCount)
                           .getResultList();
            final Long distributedResourcesCount = manager.createNamedQuery("OrganizationDistributedResources.getCountByParent", Long.class)
                                                          .setParameter("parent", organizationId)
                                                          .getSingleResult();
            return new Page<>(distributedResources.stream()
                                                  .map(OrganizationDistributedResourcesImpl::new)
                                                  .collect(Collectors.toList()),
                              skipCount,
                              maxItems,
                              distributedResourcesCount);
        } catch (RuntimeException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public void remove(String organizationId) throws ServerException {
        requireNonNull(organizationId, "Required non-null organization id");
        try {
            doRemove(organizationId);
        } catch (RuntimeException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Transactional
    protected void doRemove(String id) {
        EntityManager manager = managerProvider.get();
        OrganizationDistributedResourcesImpl distributedResources = manager.find(OrganizationDistributedResourcesImpl.class, id);
        if (distributedResources != null) {
            manager.remove(distributedResources);
        }
    }

    @Transactional
    protected void doStore(OrganizationDistributedResourcesImpl distributedResources) throws ServerException {
        EntityManager manager = managerProvider.get();
        final OrganizationDistributedResourcesImpl existingDistributedResources = manager.find(OrganizationDistributedResourcesImpl.class,
                                                                                               distributedResources.getOrganizationId());
        if (existingDistributedResources == null) {
            manager.persist(distributedResources);
        } else {
            existingDistributedResources.getResources().clear();
            existingDistributedResources.getResources().addAll(distributedResources.getResources());
        }
    }

    @Singleton
    public static class RemoveOrganizationDistributedResourcesSubscriber implements EventSubscriber<BeforeOrganizationRemovedEvent> {
        @Inject
        private EventService                        eventService;
        @Inject
        private OrganizationDistributedResourcesDao organizationDistributedResourcesDao;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this);
        }

        @Override
        public void onEvent(BeforeOrganizationRemovedEvent event) {
            try {
                organizationDistributedResourcesDao.remove(event.getOrganization().getId());
            } catch (Exception e) {
                LOG.error(format("Couldn't reset distributed organization resources before organization '%s' is removed",
                                 event.getOrganization().getId()),
                          e);
            }
        }
    }
}
