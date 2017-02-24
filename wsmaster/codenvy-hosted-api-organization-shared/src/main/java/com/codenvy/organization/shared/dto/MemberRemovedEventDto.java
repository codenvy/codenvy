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
package com.codenvy.organization.shared.dto;

import com.codenvy.organization.shared.event.EventType;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for organization member removed event.
 *
 * @author Anton Korneta
 */
@DTO
@EventOrigin("organization")
public interface MemberRemovedEventDto extends OrganizationEventDto {

    @Override
    MemberRemovedEventDto withOrganization(OrganizationDto organization);

    @Override
    MemberRemovedEventDto withType(EventType eventType);

    UserDto getMember();

    void setMember(UserDto member);

    MemberRemovedEventDto withMember(UserDto member);

    /** Returns name of user who initiated member removal */
    String getInitiator();

    void setInitiator(String initiator);

    MemberRemovedEventDto withInitiator(String initiator);

}
