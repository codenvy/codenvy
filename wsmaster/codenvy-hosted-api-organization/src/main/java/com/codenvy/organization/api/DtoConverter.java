/*
 *  [2012] - [2017] Codenvy, S.A.
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

import com.codenvy.organization.api.event.MemberAddedEvent;
import com.codenvy.organization.api.event.MemberRemovedEvent;
import com.codenvy.organization.api.event.OrganizationRemovedEvent;
import com.codenvy.organization.api.event.OrganizationRenamedEvent;
import com.codenvy.organization.shared.dto.MemberRemovedEventDto;
import com.codenvy.organization.shared.dto.OrganizationDistributedResourcesDto;
import com.codenvy.organization.shared.dto.OrganizationDto;
import com.codenvy.organization.shared.dto.OrganizationEventDto;
import com.codenvy.organization.shared.dto.MemberAddedEventDto;
import com.codenvy.organization.shared.dto.OrganizationRemovedEventDto;
import com.codenvy.organization.shared.dto.OrganizationRenamedEventDto;
import com.codenvy.organization.shared.event.OrganizationEvent;
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

    public static OrganizationRemovedEventDto asDto(OrganizationRemovedEvent event) {
        return DtoFactory.newDto(OrganizationRemovedEventDto.class)
                         .withType(event.getType())
                         .withOrganization(asDto(event.getOrganization()))
                         .withInitiator(event.getInitiator());
    }

    public static OrganizationRenamedEventDto asDto(OrganizationRenamedEvent event) {
        return DtoFactory.newDto(OrganizationRenamedEventDto.class)
                         .withType(event.getType())
                         .withOrganization(asDto(event.getOrganization()))
                         .withOldName(event.getOldName())
                         .withNewName(event.getNewName())
                         .withInitiator(event.getInitiator());
    }

    public static MemberAddedEventDto asDto(MemberAddedEvent event) {
        return DtoFactory.newDto(MemberAddedEventDto.class)
                         .withType(event.getType())
                         .withOrganization(asDto(event.getOrganization()))
                         .withInitiator(event.getInitiator())
                         .withMember(org.eclipse.che.api.user.server.DtoConverter.asDto(event.getMember()));
    }

    public static MemberRemovedEventDto asDto(MemberRemovedEvent event) {
        return DtoFactory.newDto(MemberRemovedEventDto.class)
                         .withType(event.getType())
                         .withOrganization(asDto(event.getOrganization()))
                         .withInitiator((event.getInitiator()))
                         .withMember(org.eclipse.che.api.user.server.DtoConverter.asDto(event.getMember()));
    }

    public static OrganizationEventDto asDto(OrganizationEvent event) {
        switch (event.getType()) {
            case ORGANIZATION_RENAMED:
                return asDto((OrganizationRenamedEvent)event);
            case ORGANIZATION_REMOVED:
                return asDto((OrganizationRemovedEvent)event);
            case MEMBER_ADDED:
                return asDto((MemberAddedEvent)event);
            case MEMBER_REMOVED:
                return asDto((MemberRemovedEvent)event);
            default:
                throw new IllegalArgumentException("Can't convert event to dto, event type '" + event.getType() + "' is unknown");
        }
    }
}
