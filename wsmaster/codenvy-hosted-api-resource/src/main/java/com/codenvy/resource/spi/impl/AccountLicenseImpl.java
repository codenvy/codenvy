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

import com.codenvy.resource.model.AccountLicense;
import com.codenvy.resource.model.ProvidedResources;
import com.codenvy.resource.model.Resource;
import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sergii Leschenko
 */
public class AccountLicenseImpl implements AccountLicense {
    private String                      accountId;
    private List<ProvidedResourcesImpl> resourcesDetails;
    private List<ResourceImpl>          totalResources;

    public AccountLicenseImpl(AccountLicense license) {
        this(license.getAccountId(),
             license.getResourcesDetails(),
             license.getTotalResources());
    }

    public AccountLicenseImpl(String owner,
                              List<? extends ProvidedResources> resourcesDetails,
                              List<? extends Resource> totalResources) {
        this.accountId = owner;
        if (resourcesDetails != null) {
            this.resourcesDetails = resourcesDetails.stream()
                                                    .map(ProvidedResourcesImpl::new)
                                                    .collect(Collectors.toList());
        }
        if (totalResources != null) {
            this.totalResources = totalResources.stream()
                                                .map(ResourceImpl::new)
                                                .collect(Collectors.toList());
        }
    }

    @Override
    public String getAccountId() {
        return accountId;
    }

    @Override
    public List<ProvidedResourcesImpl> getResourcesDetails() {
        if (resourcesDetails == null) {
            resourcesDetails = new ArrayList<>();
        }
        return resourcesDetails;
    }

    @Override
    public List<ResourceImpl> getTotalResources() {
        if (totalResources == null) {
            totalResources = new ArrayList<>();
        }
        return totalResources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountLicenseImpl)) return false;
        AccountLicenseImpl license = (AccountLicenseImpl)o;
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
        return "AccountLicenseImpl{" +
               "accountId='" + accountId + '\'' +
               ", resourcesDetails=" + resourcesDetails +
               ", totalResources=" + totalResources +
               '}';
    }
}
