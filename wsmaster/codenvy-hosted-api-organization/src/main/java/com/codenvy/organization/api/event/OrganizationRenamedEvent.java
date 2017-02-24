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
package com.codenvy.organization.api.event;

import com.codenvy.organization.shared.event.EventType;
import com.codenvy.organization.shared.event.OrganizationEvent;
import com.codenvy.organization.shared.model.Organization;

import static com.codenvy.organization.shared.event.EventType.ORGANIZATION_RENAMED;

/**
 * Defines organization renamed event.
 *
 * @author Anton Korneta
 */
public class OrganizationRenamedEvent implements OrganizationEvent {

    private final String       initiator;
    private final String       oldName;
    private final String       newName;
    private final Organization organization;

    public OrganizationRenamedEvent(String initiator,
                                    String oldName,
                                    String newName,
                                    Organization organization) {
        this.initiator = initiator;
        this.oldName = oldName;
        this.newName = newName;
        this.organization = organization;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    @Override
    public EventType getType() {
        return ORGANIZATION_RENAMED;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    /** Returns name of user who initiated organization rename */
    public String getInitiator() {
        return initiator;
    }

}
