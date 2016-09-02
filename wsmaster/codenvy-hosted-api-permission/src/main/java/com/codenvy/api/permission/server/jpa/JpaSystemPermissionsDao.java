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
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
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

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJpaPermissionsDao.class);

    @Inject
    public JpaSystemPermissionsDao(@Named(SystemDomain.SYSTEM_DOMAIN_ACTIONS) Set<String> allowedActions) throws IOException {
        super(new SystemDomain(allowedActions));
    }

    @Override
    public SystemPermissionsImpl get(String userId, String instanceId) throws ServerException, NotFoundException {
        return doGet(userId);
    }

    @Override
    @Transactional
    public List<SystemPermissionsImpl> getByInstance(String instanceId) throws ServerException {
        throw new ServerException("This operation is not supported for system permissions.");
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
    public static class RemoveSystemPermissionsBeforeUserRemovedEventSubscriber implements EventSubscriber<BeforeUserRemovedEvent> {
        @Inject
        private EventService eventService;
        @Inject
        JpaSystemPermissionsDao dao;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this);
        }

        @Override
        public void onEvent(BeforeUserRemovedEvent event) {
            try {
                for (SystemPermissionsImpl permissions : dao.getByUser(event.getUser().getId())) {
                    dao.remove(permissions.getUserId(), permissions.getInstanceId());
                }
            } catch (Exception x) {
                LOG.error(format("Couldn't remove permissions before user '%s' is removed", event.getUser().getId()), x);
            }
        }
    }
}
