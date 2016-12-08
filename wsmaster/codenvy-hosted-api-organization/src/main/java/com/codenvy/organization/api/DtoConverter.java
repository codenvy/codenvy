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
package com.codenvy.organization.api;

import com.codenvy.organization.shared.dto.OrganizationDistributedResourcesDto;
import com.codenvy.organization.shared.dto.OrganizationDto;
import com.codenvy.organization.shared.model.Organization;
import com.codenvy.organization.shared.model.OrganizationDistributedResources;

import org.eclipse.che.dto.server.DtoFactory;

import java.util.stream.Collectors;

/**
 * Helps to convert objects related to organization to DTOs.
 *
 * @author Sergii Leschenko
 */
public final class DtoConverter {
    private DtoConverter() {}

    public static OrganizationDto asDto(Organization organization) {
        return DtoFactory.newDto(OrganizationDto.class)
                         .withId(organization.getId())
                         .withName(organization.getName())
                         .withParent(organization.getParent());
    }

    public static OrganizationDistributedResourcesDto asDto(OrganizationDistributedResources distributedResources) {
        return DtoFactory.newDto(OrganizationDistributedResourcesDto.class)
                         .withOrganizationId(distributedResources.getOrganizationId())
                         .withResources(distributedResources.getResources()
                                                            .stream()
                                                            .map(com.codenvy.resource.api.DtoConverter::asDto)
                                                            .collect(Collectors.toList()));
    }
}
