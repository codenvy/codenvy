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

import static java.lang.String.format;

import com.codenvy.api.invite.InviteManager;
import com.codenvy.api.permission.server.PermissionsManager;
import com.codenvy.api.permission.shared.dto.PermissionsDto;
import com.codenvy.shared.invite.model.Invite;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.user.server.event.UserCreatedEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert invites to permissions when invited user registration.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class InviteToPermissionsConverter implements EventSubscriber<UserCreatedEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(InviteToPermissionsConverter.class);

  private final InviteManager inviteManager;
  private final PermissionsManager permissionsManager;

  @Inject
  public InviteToPermissionsConverter(
      InviteManager inviteManager, PermissionsManager permissionsManager) {
    this.inviteManager = inviteManager;
    this.permissionsManager = permissionsManager;
  }

  @Inject
  public void subscribe(EventService eventService) {
    eventService.subscribe(this, UserCreatedEvent.class);
  }

  @Override
  public void onEvent(UserCreatedEvent event) {
    final String createdUserId = event.getUser().getId();
    final String email = event.getUser().getEmail();
    try {
      for (Invite invite :
          Pages.iterate(
              (maxItems, skipCount) -> inviteManager.getInvites(email, maxItems, skipCount))) {
        try {
          permissionsManager.storePermission(
              DtoFactory.newDto(PermissionsDto.class)
                  .withUserId(createdUserId)
                  .withDomainId(invite.getDomainId())
                  .withInstanceId(invite.getInstanceId())
                  .withActions(invite.getActions()));
          inviteManager.remove(invite.getDomainId(), invite.getInstanceId(), email);
        } catch (ServerException | ConflictException | NotFoundException e) {
          LOG.warn(
              format(
                  "Error while accepting invite for user with id '%s' in %s with id '%s'",
                  event.getUser().getId(), invite.getDomainId(), invite.getInstanceId()),
              e);
        }
      }
    } catch (ServerException e) {
      LOG.warn(
          format("Error while fetching invites for user with id '%s'", event.getUser().getId()), e);
    }
  }
}
