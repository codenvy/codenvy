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

import org.eclipse.che.api.core.ApiException;

/**
 * @author Anatolii Bazko
 */
public interface SystemLicenseManagerObserver {
    void onCodenvyFairSourceLicenseAccepted() throws ApiException;

    void onProductLicenseDeleted(SystemLicense systemLicense) throws ApiException;

    void onProductLicenseStored(SystemLicense systemLicense) throws ApiException;
}
