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
package com.codenvy.api.license.server;

import com.codenvy.api.license.SystemLicense;
import com.codenvy.api.license.server.dao.SystemLicenseActionDao;
import com.codenvy.api.license.server.model.impl.SystemLicenseActionImpl;
import com.codenvy.api.license.shared.model.Constants;
import com.codenvy.api.license.shared.model.SystemLicenseAction;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.env.EnvironmentContext;

import java.util.Collections;

import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.Action.ADDED;
import static com.codenvy.api.license.shared.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.shared.model.Constants.Action.REMOVED;
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.PRODUCT_LICENSE;

/**
 * Handles system license actions.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class SystemLicenseActionHandler implements SystemLicenseManagerObserver {

    private final SystemLicenseActionDao dao;
    private final UserManager            userManager;


    @Inject
    public SystemLicenseActionHandler(SystemLicenseManager licenseManager,
                                      SystemLicenseActionDao dao,
                                      UserManager userManager) {
        this.dao = dao;
        this.userManager = userManager;
        licenseManager.addObserver(this);
    }

    @Override
    public void onCodenvyFairSourceLicenseAccepted() throws ApiException {
        try {
            SystemLicenseActionImpl licenseAction
                    = new SystemLicenseActionImpl(FAIR_SOURCE_LICENSE,
                                                  ACCEPTED,
                                                  System.currentTimeMillis(),
                                                  null,
                                                  Collections.emptyMap());
            dao.insert(licenseAction);
        } catch (ConflictException e) {
            throw new ConflictException("Codenvy Fair Source License has been already accepted");
        }
    }

    @Override
    public void onProductLicenseStored(SystemLicense license) throws ApiException {
        EnvironmentContext current = EnvironmentContext.getCurrent();
        String userId = current.getSubject().getUserId();
        User user = userManager.getById(userId);

        SystemLicenseActionImpl licenseAction
                = new SystemLicenseActionImpl(PRODUCT_LICENSE,
                                              ADDED,
                                              System.currentTimeMillis(),
                                              license.getLicenseId(),
                                              Collections.singletonMap("email", user.getEmail()));

        dao.remove(license.getLicenseId(), EXPIRED);  // ensure there is no record of license expired action
        dao.upsert(licenseAction);
    }

    @Override
    public void onProductLicenseExpired(SystemLicense license) throws ApiException {
        try {
            dao.getByLicenseIdAndAction(license.getLicenseId(), EXPIRED);
        } catch(NotFoundException e) {
            SystemLicenseActionImpl licenseExpiredAction
                = new SystemLicenseActionImpl(PRODUCT_LICENSE,
                                              EXPIRED,
                                              System.currentTimeMillis(),
                                              license.getLicenseId(),
                                              Collections.emptyMap());

            dao.upsert(licenseExpiredAction);
            dao.remove(PRODUCT_LICENSE, REMOVED);  // ensure there is no record of license removed action
        }
    }

    @Override
    public void onProductLicenseRemoved(SystemLicense license) throws ApiException {
        try {
            SystemLicenseAction licenseExpiredAction = dao.getByLicenseTypeAndAction(PRODUCT_LICENSE, EXPIRED);
            if (licenseExpiredAction.getLicenseId().equals(license.getLicenseId())) {
                return;  // do not add action of removing of license which had already expired before
            }
        } catch(NotFoundException e) {
            // ignore
        }

        // ensure there is no record of license expired action
        SystemLicenseActionImpl licenseAction
            = new SystemLicenseActionImpl(PRODUCT_LICENSE,
                                          REMOVED,
                                          System.currentTimeMillis(),
                                          license.getLicenseId(),
                                          Collections.emptyMap());

        dao.upsert(licenseAction);

        dao.remove(PRODUCT_LICENSE, EXPIRED);  // ensure there is no record of license expired action
    }

    /**
     * Finds license action.
     *
     * @param licenseType
     *          the type of the license
     * @param actionType
     *          the action happened with license
     * @return {@link SystemLicenseAction}
     * @throws ServerException
     *      if unexpected error occurred
     * @throws NotFoundException
     *      if no action found
     */
    public SystemLicenseAction findAction(Constants.PaidLicense licenseType, Constants.Action actionType) throws ServerException,
                                                                                                                 NotFoundException {
        return dao.getByLicenseTypeAndAction(licenseType, actionType);
    }
}
