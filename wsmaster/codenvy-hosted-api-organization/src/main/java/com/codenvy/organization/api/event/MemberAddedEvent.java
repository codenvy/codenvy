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
import com.codenvy.organization.shared.event.MemberEvent;
import com.codenvy.organization.shared.model.Organization;

import org.eclipse.che.api.core.model.user.User;

import static com.codenvy.organization.shared.event.EventType.MEMBER_ADDED;

/**
 * Defines the event of adding the organization member.
 *
 * @author Anton Korneta
 */
public class MemberAddedEvent implements MemberEvent {

    private final String       initiator;
    private final User         member;
    private final Organization organization;

    public MemberAddedEvent(String initiator,
                            User member,
                            Organization organization) {
        this.initiator = initiator;
        this.member = member;
        this.organization = organization;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    @Override
    public EventType getType() {
        return MEMBER_ADDED;
    }

    /** Returns name of user who initiated member invitation */
    public String getInitiator() {
        return initiator;
    }

    @Override
    public User getMember() {
        return member;
    }

}
