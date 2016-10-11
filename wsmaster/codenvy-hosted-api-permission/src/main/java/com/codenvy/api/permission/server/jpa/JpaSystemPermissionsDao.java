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
package com.codenvy.api.permission.server.jpa;

import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.api.permission.server.model.impl.SystemPermissionsImpl;
import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.event.CascadeRemovalEventSubscriber;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * JPA based implementation of system permissions DAO.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class JpaSystemPermissionsDao extends AbstractJpaPermissionsDao<SystemPermissionsImpl> {

    @Inject
    public JpaSystemPermissionsDao(@Named(SystemDomain.SYSTEM_DOMAIN_ACTIONS) Set<String> allowedActions) {
        super(new SystemDomain(allowedActions));
    }

    @Override
    public SystemPermissionsImpl get(String userId, String instanceId) throws ServerException, NotFoundException {
        return doGet(userId);
    }

    @Override
    @Transactional
    public List<SystemPermissionsImpl> getByInstance(String instanceId) throws ServerException {
        // instanceId is ignored because system domain doesn't require it
        try {
            return managerProvider.get()
                                  .createNamedQuery("SystemPermissions.getAll", SystemPermissionsImpl.class)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void remove(String userId, String instanceId) throws ServerException, NotFoundException {
        requireNonNull(userId, "User identifier required");
        doRemove(userId, instanceId);
    }

    @Override
    @Transactional
    public List<SystemPermissionsImpl> getByUser(String userId) throws ServerException {
        requireNonNull(userId, "User identifier required");
        try {
            return managerProvider.get()
                                  .createNamedQuery("SystemPermissions.getByUserId", SystemPermissionsImpl.class)
                                  .setParameter("userId", userId)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    protected SystemPermissionsImpl doGet(String userId) throws ServerException, NotFoundException {
        List<SystemPermissionsImpl> existent = getByUser(userId);
        if (existent.isEmpty()) {
            throw new NotFoundException(format("System permissions for user '%s' not found", userId));
        }
        return existent.get(0);
    }

    @Singleton
    public static class RemoveSystemPermissionsBeforeUserRemovedEventSubscriber
            extends CascadeRemovalEventSubscriber<BeforeUserRemovedEvent> {
        @Inject
        private EventService eventService;
        @Inject
        JpaSystemPermissionsDao dao;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this, BeforeUserRemovedEvent.class);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this, BeforeUserRemovedEvent.class);
        }

        @Override
        public void onRemovalEvent(BeforeUserRemovedEvent event) throws Exception {
            for (SystemPermissionsImpl permissions : dao.getByUser(event.getUser().getId())) {
                dao.remove(permissions.getUserId(), permissions.getInstanceId());
            }
        }
    }
}
