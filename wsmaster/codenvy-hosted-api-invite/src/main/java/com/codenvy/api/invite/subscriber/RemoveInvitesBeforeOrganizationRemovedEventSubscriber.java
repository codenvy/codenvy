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
package com.codenvy.api.invite.subscriber;

import com.codenvy.api.invite.InviteManager;
import com.codenvy.organization.api.event.BeforeOrganizationRemovedEvent;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.shared.invite.model.Invite;

import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Removes invitations that belong to organization that is going to be removed.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RemoveInvitesBeforeOrganizationRemovedEventSubscriber extends CascadeEventSubscriber<BeforeOrganizationRemovedEvent> {
    private final InviteManager inviteManager;

    @Inject
    public RemoveInvitesBeforeOrganizationRemovedEventSubscriber(InviteManager inviteManager) {
        this.inviteManager = inviteManager;
    }

    @Inject
    public void subscribe(EventService eventService) {
        eventService.subscribe(this, BeforeOrganizationRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeOrganizationRemovedEvent event) throws Exception {
        String organizationId = event.getOrganization().getId();
        for (Invite invite : Pages.iterate((maxItems, skipCount) ->
                                                   inviteManager.getInvites(OrganizationDomain.DOMAIN_ID, organizationId, skipCount,
                                                                            maxItems))) {
            inviteManager.remove(invite.getDomainId(), invite.getInstanceId(), invite.getEmail());
        }
    }
}
