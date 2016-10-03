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
package com.codenvy.resource.spi.impl;

import com.codenvy.resource.model.License;
import com.google.common.base.Objects;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
public class LicenseImpl implements License {
    private final String                      accountId;
    private final List<ProvidedResourcesImpl> resourcesDetails;
    private final List<AbstractResource>      totalResources;

    public LicenseImpl(String owner,
                       List<ProvidedResourcesImpl> resourcesDetails,
                       List<AbstractResource> totalResources) {
        this.accountId = owner;
        this.resourcesDetails = resourcesDetails;
        this.totalResources = totalResources;
    }

    @Override
    public String getAccountId() {
        return accountId;
    }

    @Override
    public List<ProvidedResourcesImpl> getResourcesDetails() {
        return resourcesDetails;
    }

    @Override
    public List<AbstractResource> getTotalResources() {
        return totalResources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LicenseImpl)) return false;
        LicenseImpl license = (LicenseImpl)o;
        return Objects.equal(accountId, license.accountId) &&
               Objects.equal(resourcesDetails, license.resourcesDetails) &&
               Objects.equal(totalResources, license.totalResources);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(accountId, resourcesDetails, totalResources);
    }

    @Override
    public String toString() {
        return "LicenseImpl{" +
               "accountId='" + accountId + '\'' +
               ", resourcesDetails=" + resourcesDetails +
               ", totalResources=" + totalResources +
               '}';
    }
}
