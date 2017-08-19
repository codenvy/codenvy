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
package com.codenvy.organization.api.listener;

import com.codenvy.api.permission.shared.event.PermissionsEvent;
import com.codenvy.api.permission.shared.model.Permissions;
import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.api.event.MemberAddedEvent;
import com.codenvy.organization.api.event.MemberRemovedEvent;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.organization.shared.model.Organization;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.user.server.UserManager;

/**
 * Maps permissions to organization related events.
 *
 * @author Anton Korneta
 */
@Singleton
public class MemberEventsPublisher implements EventSubscriber<PermissionsEvent> {

  private final EventService eventService;
  private final UserManager userManager;
  private final OrganizationManager organizationManager;

  @Inject
  public MemberEventsPublisher(
      EventService eventService, UserManager userManager, OrganizationManager organizationManager) {
    this.eventService = eventService;
    this.userManager = userManager;
    this.organizationManager = organizationManager;
  }

  @PostConstruct
  private void subscribe() {
    eventService.subscribe(this);
  }

  @Override
  public void onEvent(PermissionsEvent event) {
    final Permissions permissions = event.getPermissions();
    if (OrganizationDomain.DOMAIN_ID.equals(permissions.getDomainId())) {
      try {
        switch (event.getType()) {
          case PERMISSIONS_ADDED:
            {
              final String initiator = event.getInitiator();
              final User addedMember = userManager.getById(permissions.getUserId());
              final Organization org = organizationManager.getById(permissions.getInstanceId());
              eventService.publish(new MemberAddedEvent(initiator, addedMember, org));
              break;
            }
          case PERMISSIONS_REMOVED:
            {
              final String initiator = event.getInitiator();
              final User removedMember = userManager.getById(permissions.getUserId());
              final Organization org = organizationManager.getById(permissions.getInstanceId());
              eventService.publish(new MemberRemovedEvent(initiator, removedMember, org));
              break;
            }
          default:
            // do nothing
        }
      } catch (NotFoundException | ServerException ignored) {
      }
    }
  }
}
