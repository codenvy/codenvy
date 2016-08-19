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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author Max Shaposhnik
 */
@Singleton
public abstract class AbstractPermissionsDao<T extends AbstractPermissions> implements PermissionsDao<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPermissionsDao.class);
    private final String                       getByInstanceQuery;
    private final Class<T>                     clazz;
    private final AbstractPermissionsDomain<T> supportedDomain;


    @Inject
    private Provider<EntityManager> managerProvider;

    @Inject
    public AbstractPermissionsDao(AbstractPermissionsDomain<T> supportedDomain,
                                  Class<T> clazz,
                                  String getByInstanceQuery) throws IOException {
        this.supportedDomain = supportedDomain;
        this.getByInstanceQuery = getByInstanceQuery;
        this.clazz = clazz;
    }

    @Override
    public AbstractPermissionsDomain<T> getDomain() {
        return supportedDomain;
    }

    @Override
    public void store(T permissions) throws ServerException {
        managerProvider.get().persist(permissions);
    }

    @Override
    public T get(String userId, String instanceId) throws ServerException, NotFoundException {
        return null;
    }

    @Override
    public List<T> getByInstance(String instanceId) throws ServerException, NotFoundException {
        return managerProvider.get()
                              .createQuery(getByInstanceQuery, clazz)
                              .getResultList();
    }

    @Override
    public List<T> getByUser(String userId) throws ServerException, NotFoundException {
        return null;
    }

    @Override
    public boolean exists(String userId, String instanceId, String action) throws ServerException {
        return false;
    }

    @Override
    public void remove(String userId, String instanceId) throws ServerException, NotFoundException {

    }
}
