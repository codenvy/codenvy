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
package com.codenvy.resource.shared.dto;

import com.codenvy.resource.model.AccountLicense;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface AccountLicenseDto extends AccountLicense {
    @Override
    String getAccountId();

    void setAccountId(String accountId);

    AccountLicenseDto withAccountId(String accountId);

    @Override
    List<ProvidedResourcesDto> getResourcesDetails();

    void setResourcesDetails(List<ProvidedResourcesDto> resourcesDetails);

    AccountLicenseDto withResourcesDetails(List<ProvidedResourcesDto> resourcesDetails);

    @Override
    List<ResourceDto> getTotalResources();

    void setTotalResources(List<ResourceDto> totalResources);

    AccountLicenseDto withTotalResources(List<ResourceDto> totalResources);
}
