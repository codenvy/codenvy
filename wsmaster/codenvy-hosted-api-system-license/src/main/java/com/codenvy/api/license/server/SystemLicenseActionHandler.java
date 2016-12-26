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
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.PRODUCT_LICENSE;

/**
 * Handles system license actions.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class SystemLicenseActionHandler implements SystemLicenseManagerObserver {

    private final SystemLicenseActionDao systemLicenseActionDao;
    private final UserManager            userManager;


    @Inject
    public SystemLicenseActionHandler(SystemLicenseManager systemLicenseManager,
                                      SystemLicenseActionDao systemLicenseActionDao,
                                      UserManager userManager) {
        this.systemLicenseActionDao = systemLicenseActionDao;
        this.userManager = userManager;
        systemLicenseManager.addObserver(this);
    }

    @Override
    public void onCodenvyFairSourceLicenseAccepted() throws ApiException {
        try {
            SystemLicenseActionImpl codenvyLicenseAction
                    = new SystemLicenseActionImpl(FAIR_SOURCE_LICENSE,
                                                  ACCEPTED,
                                                  System.currentTimeMillis(),
                                                  null,
                                                  Collections.emptyMap());
            systemLicenseActionDao.insert(codenvyLicenseAction);
        } catch (ConflictException e) {
            throw new ConflictException("Codenvy Fair Source License has been already accepted");
        }
    }

    @Override
    public void onProductLicenseDeleted(SystemLicense systemLicense) throws ApiException {
        // don't update existed record about expiration of the same license
        try {
            systemLicenseActionDao.getByLicenseIdAndAction(systemLicense.getLicenseId(), EXPIRED);
        } catch (NotFoundException e) {
            SystemLicenseActionImpl codenvyLicenseAction
                = new SystemLicenseActionImpl(PRODUCT_LICENSE,
                                              EXPIRED,
                                              System.currentTimeMillis(),
                                              systemLicense.getLicenseId(),
                                              Collections.emptyMap());

            systemLicenseActionDao.upsert(codenvyLicenseAction);
        }
    }

    @Override
    public void onProductLicenseStored(SystemLicense systemLicense) throws ApiException {
        EnvironmentContext current = EnvironmentContext.getCurrent();
        String userId = current.getSubject().getUserId();
        User user = userManager.getById(userId);

        SystemLicenseActionImpl codenvyLicenseAction
                = new SystemLicenseActionImpl(PRODUCT_LICENSE,
                                              ADDED,
                                              System.currentTimeMillis(),
                                              systemLicense.getLicenseId(),
                                              Collections.singletonMap("email", user.getEmail()));

        systemLicenseActionDao.upsert(codenvyLicenseAction);
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
        return systemLicenseActionDao.getByLicenseTypeAndAction(licenseType, actionType);
    }
}
