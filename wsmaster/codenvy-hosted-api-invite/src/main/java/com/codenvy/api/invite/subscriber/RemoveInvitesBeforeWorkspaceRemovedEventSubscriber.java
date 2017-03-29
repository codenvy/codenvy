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
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.shared.invite.model.Invite;

import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Removes invitations that belong to workspace that is going to be removed.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RemoveInvitesBeforeWorkspaceRemovedEventSubscriber extends CascadeEventSubscriber<BeforeWorkspaceRemovedEvent> {
    private final InviteManager inviteManager;

    @Inject
    public RemoveInvitesBeforeWorkspaceRemovedEventSubscriber(InviteManager inviteManager) {
        this.inviteManager = inviteManager;
    }

    @Inject
    public void subscribe(EventService eventService) {
        eventService.subscribe(this, BeforeWorkspaceRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeWorkspaceRemovedEvent event) throws Exception {
        String workspaceId = event.getWorkspace().getId();
        for (Invite invite : Pages.iterate((maxItems, skipCount) ->
                                                   inviteManager.getInvites(WorkspaceDomain.DOMAIN_ID, workspaceId, skipCount, maxItems))) {
            inviteManager.remove(invite.getDomainId(), invite.getInstanceId(), invite.getEmail());
        }
    }
}
