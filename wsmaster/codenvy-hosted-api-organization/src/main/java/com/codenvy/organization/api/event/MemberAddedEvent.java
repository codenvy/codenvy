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
