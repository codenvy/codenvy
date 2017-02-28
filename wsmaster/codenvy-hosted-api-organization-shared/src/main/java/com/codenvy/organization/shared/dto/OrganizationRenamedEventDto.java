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
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for organization renamed event.
 *
 * @author Anton Korneta
 */
@DTO
@EventOrigin("organization")
public interface OrganizationRenamedEventDto extends OrganizationEventDto {

    @Override
    OrganizationRenamedEventDto withOrganization(OrganizationDto organization);

    @Override
    OrganizationRenamedEventDto withType(EventType eventType);

    /** Returns organization name before renaming */
    String getOldName();

    void setOldName(String oldName);

    OrganizationRenamedEventDto withOldName(String oldName);

    /** Returns organization name after renaming */
    String getNewName();

    void setNewName(String newName);

    OrganizationRenamedEventDto withNewName(String newName);

    /** Returns name of user who initiated organization rename */
    String getInitiator();

    void setInitiator(String initiator);

    OrganizationRenamedEventDto withInitiator(String initiator);

}
