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
