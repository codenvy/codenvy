/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.license.server.jpa;

import com.codenvy.api.license.server.dao.SystemLicenseActionDao;
import com.codenvy.api.license.server.model.impl.SystemLicenseActionImpl;
import com.codenvy.api.license.shared.model.Constants;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import static java.util.Objects.requireNonNull;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class JpaSystemLicenseActionDao implements SystemLicenseActionDao {

    @Inject
    protected Provider<EntityManager> managerProvider;

    @Override
    public void insert(SystemLicenseActionImpl systemLicenseAction) throws ServerException, ConflictException {
        requireNonNull(systemLicenseAction, "Required non-null system license action");

        try {
            doInsert(systemLicenseAction);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("System license action already exists");
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public void upsert(SystemLicenseActionImpl systemLicenseAction) throws ServerException, ConflictException {
        requireNonNull(systemLicenseAction, "Required non-null system license action");

        try {
            doUpsert(systemLicenseAction);
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public void remove(Constants.PaidLicense licenseType, Constants.Action actionType) throws ServerException {
        requireNonNull(licenseType, "Required non-null system license type");
        requireNonNull(actionType, "Required non-null system action type");

        try {
            doRemove(licenseType, actionType);
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Override public void remove(String licenseId, Constants.Action actionType) throws ServerException {
        requireNonNull(licenseId, "Required non-null system license id");
        requireNonNull(actionType, "Required non-null system action type");

        try {
            doRemove(licenseId, actionType);
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Override
    @Transactional
    public SystemLicenseActionImpl getByLicenseTypeAndAction(Constants.PaidLicense licenseType, Constants.Action actionType) throws ServerException,
                                                                                                                                    NotFoundException {
        requireNonNull(licenseType, "Required non-null system license type");
        requireNonNull(actionType, "Required non-null system action type");

        try {
            return managerProvider.get()
                                  .createNamedQuery("LicenseAction.getByLicenseTypeAndAction", SystemLicenseActionImpl.class)
                                  .setParameter("license_type", licenseType)
                                  .setParameter("action_type", actionType)
                                  .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("System license action not found");
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Override
    @Transactional
    public SystemLicenseActionImpl getByLicenseIdAndAction(String licenseId, Constants.Action actionType) throws ServerException,
                                                                                                                 NotFoundException {
        requireNonNull(licenseId, "Required non-null system license id");
        requireNonNull(actionType, "Required non-null system action type");

        try {
            return managerProvider.get()
                                  .createNamedQuery("LicenseAction.getByLicenseIdAndAction", SystemLicenseActionImpl.class)
                                  .setParameter("license_id", licenseId)
                                  .setParameter("action_type", actionType)
                                  .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("System license action not found");
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Transactional
    protected void doInsert(SystemLicenseActionImpl codenvyLicenseAction) {
        managerProvider.get().persist(codenvyLicenseAction);
    }

    @Transactional
    protected void doRemove(Constants.PaidLicense licenseType, Constants.Action actionType) throws ServerException {
        try {
            SystemLicenseActionImpl action = getByLicenseTypeAndAction(licenseType, actionType);
            managerProvider.get().remove(action);
        } catch (NotFoundException ignored) {
        }
    }

    @Transactional
    protected void doRemove(String licenseId, Constants.Action actionType) throws ServerException {
        try {
            SystemLicenseActionImpl action = getByLicenseIdAndAction(licenseId, actionType);
            managerProvider.get().remove(action);
        } catch (NotFoundException ignored) {
        }
    }

    @Transactional
    protected void doUpsert(SystemLicenseActionImpl codenvyLicenseAction) {
        EntityManager entityManager = managerProvider.get();
        try {
            entityManager.createNamedQuery("LicenseAction.getByLicenseTypeAndAction", SystemLicenseActionImpl.class)
                         .setParameter("license_type", codenvyLicenseAction.getLicenseType())
                         .setParameter("action_type", codenvyLicenseAction.getActionType())
                         .getSingleResult();
            entityManager.merge(codenvyLicenseAction);
        } catch (NoResultException e) {
            entityManager.persist(codenvyLicenseAction);
        }
    }
}
