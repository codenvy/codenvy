/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.spi.invite.jpa;

import com.codenvy.api.invite.InviteImpl;
import com.codenvy.spi.invite.InviteDao;
import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * JPA implementation of {@link InviteDao}.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class JpaInviteDao implements InviteDao {
    private final Provider<EntityManager> managerProvider;

    @Inject
    public JpaInviteDao(Provider<EntityManager> managerProvider) {
        this.managerProvider = managerProvider;
    }

    @Override
    public Optional<InviteImpl> store(InviteImpl invite) throws ServerException {
        requireNonNull(invite, "Required non-null invite");
        try {
            return doCreate(invite);
        } catch (RuntimeException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public Page<InviteImpl> getInvites(String email, long skipCount, int maxItems) throws ServerException {
        requireNonNull(email, "Required non-null email");
        checkArgument(skipCount <= Integer.MAX_VALUE, "The number of items to skip can't be greater than " + Integer.MAX_VALUE);
        try {
            final EntityManager manager = managerProvider.get();
            List<InviteImpl> result = managerProvider.get()
                                                     .createNamedQuery("Invite.getByEmail", InviteImpl.class)
                                                     .setParameter("email", email)
                                                     .setFirstResult((int)skipCount)
                                                     .setMaxResults(maxItems)
                                                     .getResultList()
                                                     .stream()
                                                     .map(InviteImpl::new)
                                                     .collect(Collectors.toList());

            final long invitesCount = manager.createNamedQuery("Invite.getByEmailCount", Long.class)
                                             .setParameter("email", email)
                                             .getSingleResult();

            return new Page<>(result, skipCount, maxItems, invitesCount);
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    @Override
    public Page<InviteImpl> getInvites(String domainId, String instanceId, long skipCount, int maxItems) throws ServerException {
        requireNonNull(domainId, "Required non-null domain");
        requireNonNull(instanceId, "Required non-null instance");
        checkArgument(skipCount <= Integer.MAX_VALUE, "The number of items to skip can't be greater than " + Integer.MAX_VALUE);
        try {
            final EntityManager manager = managerProvider.get();
            List<InviteImpl> result = managerProvider.get()
                                                     .createNamedQuery("Invite.getByInstance", InviteImpl.class)
                                                     .setParameter("domain", domainId)
                                                     .setParameter("instance", instanceId)
                                                     .setFirstResult((int)skipCount)
                                                     .setMaxResults(maxItems)
                                                     .getResultList()
                                                     .stream()
                                                     .map(InviteImpl::new)
                                                     .collect(Collectors.toList());

            final long invitationsCount = manager.createNamedQuery("Invite.getByInstanceCount", Long.class)
                                                 .setParameter("domain", domainId)
                                                 .setParameter("instance", instanceId)
                                                 .getSingleResult();

            return new Page<>(result, skipCount, maxItems, invitationsCount);
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    @Override
    public InviteImpl getInvite(String domainId, String instanceId, String email) throws NotFoundException, ServerException {
        requireNonNull(domainId, "Required non-null domain");
        requireNonNull(instanceId, "Required non-null instance");
        requireNonNull(email, "Required non-null email");
        try {
            return new InviteImpl(getEntity(domainId, instanceId, email));
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void remove(String domainId, String instanceId, String email) throws ServerException {
        requireNonNull(domainId, "Required non-null domain");
        requireNonNull(instanceId, "Required non-null instance");
        requireNonNull(email, "Required non-null email");
        try {
            doRemove(domainId, instanceId, email);
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    protected void doRemove(String domain, String instance, String email) {
        try {
            InviteImpl invite = getEntity(domain, instance, email);
            final EntityManager manager = managerProvider.get();
            manager.remove(invite);
            manager.flush();
        } catch (NotFoundException e) {
            //invite is already removed
        }
    }

    @Transactional
    protected Optional<InviteImpl> doCreate(InviteImpl invite) throws ServerException {
        EntityManager manager = managerProvider.get();
        InviteImpl existing = null;
        try {
            final InviteImpl result = getEntity(invite.getDomainId(), invite.getInstanceId(), invite.getEmail());
            existing = new InviteImpl(result);
            result.getActions().clear();
            result.getActions().addAll(invite.getActions());
        } catch (NotFoundException n) {
            manager.persist(invite);
        }
        manager.flush();
        return Optional.ofNullable(existing);
    }

    private InviteImpl getEntity(String domain, String instance, String email) throws NotFoundException {
        try {
            return managerProvider.get()
                                  .createNamedQuery("Invite.get", InviteImpl.class)
                                  .setParameter("domain", domain)
                                  .setParameter("instance", instance)
                                  .setParameter("email", email)
                                  .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException(format("Invitation to %s with id %s for user with email %s and was not found",
                                               domain, instance, email));
        }
    }
}
