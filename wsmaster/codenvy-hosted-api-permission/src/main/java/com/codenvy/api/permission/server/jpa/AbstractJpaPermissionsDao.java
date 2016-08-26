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

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.server.spi.PermissionsDao;
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
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author Max Shaposhnik
 */
public abstract class AbstractJpaPermissionsDao<T extends AbstractPermissions> implements PermissionsDao<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJpaPermissionsDao.class);
    private final AbstractPermissionsDomain<T> supportedDomain;


    @Inject
    protected Provider<EntityManager> managerProvider;

    public AbstractJpaPermissionsDao(AbstractPermissionsDomain<T> supportedDomain) throws IOException {
        this.supportedDomain = supportedDomain;
    }

    @Override
    public AbstractPermissionsDomain<T> getDomain() {
        return supportedDomain;
    }

    @Override
    public void store(T permissions) throws ServerException {
        requireNonNull(permissions, "Permissions instance required");
        doCreate(permissions);
    }

    @Override
    public abstract T get(String userId, String instanceId) throws ServerException, NotFoundException;

    @Override
    public abstract List<T> getByInstance(String instanceId) throws ServerException;

    @Override
    public abstract List<T> getByUser(String userId) throws ServerException;

    @Override
    @Transactional
    public boolean exists(String userId, String instanceId, String action) throws ServerException {
        requireNonNull(instanceId, "Instance identifier required");
        requireNonNull(userId, "User identifier required");
        requireNonNull(action, "Action name required");
        T permissions;
        try {
            permissions = get(userId, instanceId);
        } catch (NotFoundException e) {
            return false;
        }
        return permissions.getActions().contains(action);
    }

    @Override
    public void remove(String userId, String instanceId) throws ServerException, NotFoundException {
        requireNonNull(instanceId, "Instance identifier required");
        requireNonNull(userId, "User identifier required");
        doRemove(userId, instanceId);
    }

    @Transactional
    protected void doCreate(T permissions) throws ServerException {
        EntityManager manager = managerProvider.get();
        try {
            final T result = get(permissions.getUserId(), permissions.getInstanceId());
            result.getActions().clear();
            result.getActions().addAll(permissions.getActions());
        } catch (NotFoundException n) {
            manager.persist(permissions);
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    protected void doRemove(String userId, String instanceId) throws ServerException, NotFoundException {
        try {
            final T entity = get(userId, instanceId);
            managerProvider.get().remove(entity);
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }


    @Singleton
    public static class RemovePermissionsBeforeUserRemovedEventSubscriber implements EventSubscriber<BeforeUserRemovedEvent> {
        @Inject
        private EventService eventService;
        @Inject
        Set<PermissionsDao<? extends AbstractPermissions>> storages;

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
                for (PermissionsDao<? extends AbstractPermissions> dao : storages) {
                    for (AbstractPermissions permissions : dao.getByUser(event.getUser().getId())) {
                        dao.remove(permissions.getUserId(), permissions.getInstanceId());
                    }
                }
            } catch (Exception x) {
                LOG.error(format("Couldn't remove workers before user '%s' is removed", event.getUser().getId()), x);
            }
        }
    }

}
