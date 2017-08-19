/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.api.invite.subscriber;

import com.codenvy.api.invite.InviteManager;
import com.codenvy.organization.api.event.BeforeOrganizationRemovedEvent;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.shared.invite.model.Invite;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;

/**
 * Removes invitations that belong to organization that is going to be removed.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RemoveInvitesBeforeOrganizationRemovedEventSubscriber
    extends CascadeEventSubscriber<BeforeOrganizationRemovedEvent> {
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
    for (Invite invite :
        Pages.iterate(
            (maxItems, skipCount) ->
                inviteManager.getInvites(
                    OrganizationDomain.DOMAIN_ID, organizationId, skipCount, maxItems))) {
      inviteManager.remove(invite.getDomainId(), invite.getInstanceId(), invite.getEmail());
    }
  }
}
