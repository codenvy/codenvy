/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.organization.shared.dto;

import com.codenvy.organization.shared.event.EventType;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for member added event.
 *
 * @author Anton Korneta
 */
@DTO
@EventOrigin("organization")
public interface MemberAddedEventDto extends OrganizationEventDto {

    @Override
    MemberAddedEventDto withOrganization(OrganizationDto organization);

    @Override
    MemberAddedEventDto withType(EventType eventType);

    UserDto getMember();

    void setMember(UserDto member);

    MemberAddedEventDto withMember(UserDto member);

    /** Returns name of user who initiated member invitation */
    String getInitiator();

    void setInitiator(String initiator);

    MemberAddedEventDto withInitiator(String initiator);

}
