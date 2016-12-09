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
package com.codenvy.api.license.server.jpa;

import com.codenvy.api.license.model.Constants;
import com.codenvy.api.license.server.dao.CodenvyLicenseActionDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
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
public class JpaCodenvyLicenseActionDao implements CodenvyLicenseActionDao {

    @Inject
    protected Provider<EntityManager> managerProvider;

    @Override
    public void store(CodenvyLicenseActionImpl codenvyLicenseAction) throws ServerException, ConflictException {
        requireNonNull(codenvyLicenseAction, "Required non-null codenvy license action");

        try {
            doStore(codenvyLicenseAction);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("Codenvy license action already exists");
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public void remove(Constants.License licenseType, Constants.Action actionType) throws ServerException {
        try {
            doRemove(licenseType, actionType);
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Override
    @Transactional
    public CodenvyLicenseActionImpl getByLicenseAndAction(Constants.License licenseType, Constants.Action actionType) throws ServerException,
                                                                                                                             NotFoundException {
        try {
            return managerProvider.get()
                                  .createNamedQuery("LicenseAction.getByLicenseAndAction", CodenvyLicenseActionImpl.class)
                                  .setParameter("license_type", licenseType)
                                  .setParameter("action_type", actionType)
                                  .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("Codenvy license action not found");
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Transactional
    protected void doStore(CodenvyLicenseActionImpl codenvyLicenseAction) {
        managerProvider.get().persist(codenvyLicenseAction);
    }

    @Transactional
    protected void doRemove(Constants.License licenseType, Constants.Action licenseAction) throws ServerException {
        try {
            CodenvyLicenseActionImpl action = getByLicenseAndAction(licenseType, licenseAction);
            managerProvider.get().remove(action);
        } catch (NotFoundException e) {
            return;
        }
    }
}
