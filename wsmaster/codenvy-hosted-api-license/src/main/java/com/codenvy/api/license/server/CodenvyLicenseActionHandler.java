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

import com.codenvy.api.license.CodenvyLicense;
import com.codenvy.api.license.server.dao.CodenvyLicenseActionDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
import com.codenvy.api.license.shared.model.FairSourceLicenseAcceptance;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.shared.model.Constants.License.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.shared.model.Constants.License.PRODUCT_LICENSE;

/**
 * Handles Codenvy license actions.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class CodenvyLicenseActionHandler implements CodenvyLicenseManagerObserver {

    private final CodenvyLicenseActionDao codenvyLicenseActionDao;

    @Inject
    public CodenvyLicenseActionHandler(CodenvyLicenseManager codenvyLicenseManager, CodenvyLicenseActionDao codenvyLicenseActionDao) {
        this.codenvyLicenseActionDao = codenvyLicenseActionDao;
        codenvyLicenseManager.addObserver(this);
    }

    @Override
    public void onCodenvyFairSourceLicenseAccepted(FairSourceLicenseAcceptance fairSourceLicenseAcceptance) throws ApiException {
        Map<String, String> attributes = new HashMap<>(3);
        attributes.put("firstName", fairSourceLicenseAcceptance.getFirstName());
        attributes.put("lastName", fairSourceLicenseAcceptance.getLastName());
        attributes.put("email", fairSourceLicenseAcceptance.getEmail());

        try {
            CodenvyLicenseActionImpl codenvyLicenseAction
                    = new CodenvyLicenseActionImpl(FAIR_SOURCE_LICENSE,
                                                   ACCEPTED,
                                                   System.currentTimeMillis(),
                                                   null,
                                                   attributes);
            codenvyLicenseActionDao.insert(codenvyLicenseAction);
        } catch (ConflictException e) {
            throw new ConflictException("Codenvy Fair Source License has been already accepted");
        }
    }

    @Override
    public void onProductLicenseDeleted(CodenvyLicense codenvyLicense) throws ApiException {
        CodenvyLicenseActionImpl codenvyLicenseAction
                = new CodenvyLicenseActionImpl(PRODUCT_LICENSE,
                                               EXPIRED,
                                               System.currentTimeMillis(),
                                               codenvyLicense.getLicenseId(),
                                               Collections.emptyMap());

        codenvyLicenseActionDao.upsert(codenvyLicenseAction);
    }

    @Override
    public void onProductLicenseStored(CodenvyLicense codenvyLicense) throws ApiException {
        try {
            CodenvyLicenseActionImpl prevCodenvyLicenseAction = codenvyLicenseActionDao.getByLicenseAndAction(PRODUCT_LICENSE, ACCEPTED);
            codenvyLicenseActionDao.remove(PRODUCT_LICENSE, EXPIRED);

            if (prevCodenvyLicenseAction.getLicenseQualifier().equalsIgnoreCase(codenvyLicense.getLicenseId())) {
                return;
            }
        } catch (NotFoundException ignored) {
        }

        CodenvyLicenseActionImpl codenvyLicenseAction
                = new CodenvyLicenseActionImpl(PRODUCT_LICENSE,
                                               ACCEPTED,
                                               System.currentTimeMillis(),
                                               codenvyLicense.getLicenseId(),
                                               Collections.emptyMap());

        codenvyLicenseActionDao.upsert(codenvyLicenseAction);
    }
}
