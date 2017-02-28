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
import com.codenvy.organization.shared.model.Member;
import com.codenvy.organization.shared.model.Organization;

import java.util.List;

import static com.codenvy.organization.shared.event.EventType.ORGANIZATION_REMOVED;

/**
 * Defines organization removed event.
 *
 * @author Anton Korneta
 */
public class OrganizationRemovedEvent implements OrganizationEvent {

    private final String                 initiator;
    private final Organization           organization;
    private final List<? extends Member> members;

    public OrganizationRemovedEvent(String initiator,
                                    Organization organization,
                                    List<? extends Member> members) {
        this.initiator = initiator;
        this.organization = organization;
        this.members = members;
    }

    @Override
    public EventType getType() {
        return ORGANIZATION_REMOVED;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    public List<? extends Member> getMembers() {
        return members;
    }

    /** Returns name of user who initiated organization removal */
    public String getInitiator() {
        return initiator;
    }

}
