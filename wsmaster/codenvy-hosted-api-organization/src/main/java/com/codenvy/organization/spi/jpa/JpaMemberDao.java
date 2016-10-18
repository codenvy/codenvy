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

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.jpa.AbstractJpaPermissionsDao;
import com.codenvy.organization.api.event.BeforeOrganizationRemovedEvent;
import com.codenvy.organization.spi.MemberDao;
import com.codenvy.organization.spi.impl.MemberImpl;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.event.CascadeRemovalEventSubscriber;
import org.eclipse.che.api.core.notification.EventService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * JPA based implementation of {@link MemberDao}.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class JpaMemberDao extends AbstractJpaPermissionsDao<MemberImpl> implements MemberDao {

    @Inject
    public JpaMemberDao(AbstractPermissionsDomain<MemberImpl> supportedDomain) throws IOException {
        super(supportedDomain);
    }

    @Override
    public MemberImpl get(String userId, String instanceId) throws ServerException, NotFoundException {
        return getMember(instanceId, userId);
    }

    @Override
    public List<MemberImpl> getByInstance(String instanceId) throws ServerException {
        return getMembers(instanceId);
    }

    @Override
    public List<MemberImpl> getByUser(String userId) throws ServerException {
        return getMemberships(userId);
    }

    @Override
    public void remove(String userId, String organizationId) throws ServerException {
        requireNonNull(organizationId, "Required non-null organization id");
        requireNonNull(userId, "Required non-null user id");
        try {
            doRemove(organizationId, userId);
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public MemberImpl getMember(String organizationId, String userId) throws NotFoundException, ServerException {
        requireNonNull(organizationId, "Required non-null organization id");
        requireNonNull(userId, "Required non-null user id");
        try {
            final EntityManager manager = managerProvider.get();
            return manager.createNamedQuery("Member.getMember", MemberImpl.class)
                          .setParameter("userId", userId)
                          .setParameter("organizationId", organizationId)
                          .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException(String.format("Membership of user %s in organization %s was not found", userId, organizationId));
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<MemberImpl> getMembers(String organizationId) throws ServerException {
        requireNonNull(organizationId, "Required non-null organization id");
        try {
            final EntityManager manager = managerProvider.get();
            return manager.createNamedQuery("Member.getByOrganization", MemberImpl.class)
                          .setParameter("organizationId", organizationId)
                          .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<MemberImpl> getMemberships(String userId) throws ServerException {
        requireNonNull(userId, "Required non-null user id");
        try {
            final EntityManager manager = managerProvider.get();
            return manager.createNamedQuery("Member.getByUser", MemberImpl.class)
                          .setParameter("userId", userId)
                          .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public Page<OrganizationImpl> getOrganizations(String userId, int maxItems, int skipCount) throws ServerException {
        requireNonNull(userId, "Required non-null user id");
        try {
            final EntityManager manager = managerProvider.get();
            final List<OrganizationImpl> result = manager.createNamedQuery("Member.getOrganizations", OrganizationImpl.class)
                                                         .setParameter("userId", userId)
                                                         .setMaxResults(maxItems)
                                                         .setFirstResult(skipCount)
                                                         .getResultList();
            final Long organizationsCount = manager.createNamedQuery("Member.getOrganizationsCount", Long.class)
                                                   .setParameter("userId", userId)
                                                   .getSingleResult();

            return new Page<>(result, skipCount, maxItems, organizationsCount);
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    protected void doRemove(String organizationId, String userId) {
        final EntityManager manager = managerProvider.get();
        List<MemberImpl> members = manager.createNamedQuery("Member.getMember", MemberImpl.class)
                                          .setParameter("userId", userId)
                                          .setParameter("organizationId", organizationId)
                                          .getResultList();
        if (!members.isEmpty()) {
            manager.remove(members.get(0));
        }
    }

    @Singleton
    public static class RemoveMembersBeforeOrganizationRemovedEventSubscriber
            extends CascadeRemovalEventSubscriber<BeforeOrganizationRemovedEvent> {
        @Inject
        private EventService eventService;
        @Inject
        private MemberDao    memberDao;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this, BeforeOrganizationRemovedEvent.class);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this, BeforeOrganizationRemovedEvent.class);
        }

        @Override
        public void onRemovalEvent(BeforeOrganizationRemovedEvent event) throws Exception {
            for (MemberImpl member : memberDao.getMembers(event.getOrganization().getId())) {
                memberDao.remove(member.getUserId(), member.getOrganizationId());
            }
        }
    }
}
