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

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codenvy.api.invite.InviteImpl;
import com.codenvy.api.invite.InviteManager;
import com.codenvy.organization.api.event.BeforeOrganizationRemovedEvent;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.notification.EventService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link RemoveInvitesBeforeOrganizationRemovedEventSubscriber}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class RemoveInvitesBeforeOrganizationRemovedEventSubscriberTest {
  private static final String ORG_ID = "orgId";
  private static final String EMAIL_1 = "user@test.com";
  private static final String EMAIL_2 = "user2@test.com";

  @Mock private EventService eventService;

  @Mock private InviteManager inviteManager;

  @InjectMocks private RemoveInvitesBeforeOrganizationRemovedEventSubscriber subscriber;

  @Test
  public void shouldSubscribeItself() {
    subscriber.subscribe(eventService);

    verify(eventService).subscribe(subscriber, BeforeOrganizationRemovedEvent.class);
  }

  @Test
  public void shouldRemoveInvitesOnBeforeOrganizationEvent() throws Exception {
    OrganizationImpl toRemove = new OrganizationImpl("orgId", "orgName", null);
    InviteImpl invite1 =
        new InviteImpl(
            EMAIL_1, OrganizationDomain.DOMAIN_ID, ORG_ID, OrganizationDomain.getActions());
    InviteImpl invite2 =
        new InviteImpl(
            EMAIL_2,
            OrganizationDomain.DOMAIN_ID,
            ORG_ID,
            singletonList(OrganizationDomain.CREATE_WORKSPACES));
    doReturn(new Page<>(singletonList(invite1), 0, 1, 2))
        .doReturn(new Page<>(singletonList(invite2), 1, 1, 2))
        .when(inviteManager)
        .getInvites(anyString(), anyString(), anyLong(), anyInt());

    subscriber.onCascadeEvent(new BeforeOrganizationRemovedEvent(toRemove));

    verify(inviteManager, times(2))
        .getInvites(eq(OrganizationDomain.DOMAIN_ID), eq(ORG_ID), anyLong(), anyInt());
    verify(inviteManager).remove(OrganizationDomain.DOMAIN_ID, ORG_ID, EMAIL_1);
    verify(inviteManager).remove(OrganizationDomain.DOMAIN_ID, ORG_ID, EMAIL_2);
  }
}
