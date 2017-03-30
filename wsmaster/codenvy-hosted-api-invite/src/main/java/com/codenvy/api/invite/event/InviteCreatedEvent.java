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
package com.codenvy.api.invite.event;

import com.codenvy.shared.invite.model.Invite;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Propagates when invite created.
 *
 * @author Sergii Leschenko
 */
public class InviteCreatedEvent {
    private String initiatorId;
    private Invite invite;

    public InviteCreatedEvent(String initiatorId, Invite invite) {
        this.initiatorId = initiatorId;
        this.invite = invite;
    }

    /**
     * Returns id of user who sent invite or null if user is undefined.
     */
    @Nullable
    public String getInitiatorId() {
        return initiatorId;
    }

    /**
     * Returns created invite.
     */
    public Invite getInvite() {
        return invite;
    }
}
