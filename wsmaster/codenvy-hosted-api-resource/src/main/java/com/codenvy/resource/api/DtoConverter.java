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
package com.codenvy.resource.api;

import com.codenvy.resource.model.AccountLicense;
import com.codenvy.resource.model.FreeResourcesLimit;
import com.codenvy.resource.model.ProvidedResources;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.shared.dto.AccountLicenseDto;
import com.codenvy.resource.shared.dto.FreeResourcesLimitDto;
import com.codenvy.resource.shared.dto.ProvidedResourcesDto;
import com.codenvy.resource.shared.dto.ResourceDto;

import org.eclipse.che.dto.server.DtoFactory;

import java.util.stream.Collectors;

/**
 * Helps to convert objects related to resource to DTOs.
 *
 * @author Sergii Leschenko
 */
public final class DtoConverter {
    private DtoConverter() {}

    public static ResourceDto asDto(Resource resource) {
        return DtoFactory.newDto(ResourceDto.class)
                         .withAmount(resource.getAmount())
                         .withType(resource.getType())
                         .withUnit(resource.getUnit());
    }

    public static FreeResourcesLimitDto asDto(FreeResourcesLimit limit) {
        return DtoFactory.newDto(FreeResourcesLimitDto.class)
                         .withResources(limit.getResources()
                                             .stream()
                                             .map(DtoConverter::asDto)
                                             .collect(Collectors.toList()))
                         .withAccountId(limit.getAccountId());
    }

    public static AccountLicenseDto asDto(AccountLicense license) {
        return DtoFactory.newDto(AccountLicenseDto.class)
                         .withAccountId(license.getAccountId())
                         .withTotalResources(license.getTotalResources()
                                                    .stream()
                                                    .map(DtoConverter::asDto)
                                                    .collect(Collectors.toList()))
                         .withResourcesDetails(license.getResourcesDetails()
                                                      .stream()
                                                      .map(DtoConverter::asDto)
                                                      .collect(Collectors.toList()));
    }

    private static ProvidedResourcesDto asDto(ProvidedResources providedResources) {
        return DtoFactory.newDto(ProvidedResourcesDto.class)
                         .withId(providedResources.getId())
                         .withOwner(providedResources.getOwner())
                         .withStartTime(providedResources.getStartTime())
                         .withEndTime(providedResources.getEndTime())
                         .withProviderId(providedResources.getProviderId())
                         .withResources(providedResources.getResources()
                                                         .stream()
                                                         .map(DtoConverter::asDto)
                                                         .collect(Collectors.toList()));
    }
}
