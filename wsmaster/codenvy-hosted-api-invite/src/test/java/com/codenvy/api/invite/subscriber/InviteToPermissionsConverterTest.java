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

import com.codenvy.api.invite.InviteImpl;
import com.codenvy.api.invite.InviteManager;
import com.codenvy.api.permission.server.PermissionsManager;
import com.codenvy.api.permission.shared.dto.PermissionsDto;
import com.codenvy.api.permission.shared.model.Permissions;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.shared.invite.model.Invite;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.event.UserCreatedEvent;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link InviteToPermissionsConverter}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class InviteToPermissionsConverterTest {
    private static final String USER_EMAIL = "user@test.com";
    private static final String USER_ID    = "user123";

    @Mock
    private EventService eventService;

    @Mock
    private InviteManager inviteManager;

    @Mock
    private PermissionsManager permissionsManager;

    @InjectMocks
    private InviteToPermissionsConverter converter;

    @Test
    public void shouldSubscribeItself() {
        converter.subscribe(eventService);

        verify(eventService).subscribe(converter, UserCreatedEvent.class);
    }

    @Test
    public void shouldConvertInvitesToPermissionsWhenUserCreated() throws Exception {
        InviteImpl invite1 = new InviteImpl(USER_EMAIL, OrganizationDomain.DOMAIN_ID, "org123", asList("read", "update"));
        InviteImpl invite2 = new InviteImpl(USER_EMAIL, WorkspaceDomain.DOMAIN_ID, "ws321", singletonList("delete"));
        Permissions expectedPermission1 = convert(invite1);
        Permissions expectedPermission2 = convert(invite2);
        doReturn(new Page<>(singletonList(invite1), 0, 1, 2))
                .doReturn(new Page<>(singletonList(invite2), 1, 1, 2))
                .when(inviteManager).getInvites(anyString(), anyInt(), anyLong());

        converter.onEvent(new UserCreatedEvent(DtoFactory.newDto(UserDto.class)
                                                         .withEmail(USER_EMAIL)
                                                         .withId(USER_ID)));

        verify(inviteManager, times(2)).getInvites(eq(USER_EMAIL), anyInt(), anyLong());
        verify(inviteManager).remove(invite1.getDomainId(), invite1.getInstanceId(), USER_EMAIL);
        verify(inviteManager).remove(invite2.getDomainId(), invite2.getInstanceId(), USER_EMAIL);
        verify(permissionsManager).storePermission(expectedPermission1);
        verify(permissionsManager).storePermission(expectedPermission2);
    }

    @Test
    public void shouldSkipInviteWhenExceptionOccursOnInviteConverting() throws Exception {
        InviteImpl invite1 = new InviteImpl(USER_EMAIL, OrganizationDomain.DOMAIN_ID, "org123", asList("read", "update"));
        InviteImpl invite2 = new InviteImpl(USER_EMAIL, WorkspaceDomain.DOMAIN_ID, "ws321", singletonList("delete"));
        Permissions expectedPermission2 = convert(invite2);
        doReturn(new Page<>(singletonList(invite1), 0, 1, 2))
                .doReturn(new Page<>(singletonList(invite2), 1, 1, 2))
                .when(inviteManager).getInvites(anyString(), anyInt(), anyLong());
        doThrow(new ConflictException(""))
                .doNothing()
                .when(permissionsManager).storePermission(any());

        converter.onEvent(new UserCreatedEvent(DtoFactory.newDto(UserDto.class)
                                                         .withEmail(USER_EMAIL)
                                                         .withId(USER_ID)));

        verify(inviteManager, times(2)).getInvites(eq(USER_EMAIL), anyInt(), anyLong());
        verify(inviteManager).remove(invite2.getDomainId(), invite2.getInstanceId(), USER_EMAIL);
        verify(permissionsManager).storePermission(expectedPermission2);
    }

    private Permissions convert(Invite invite) {
        return DtoFactory.newDto(PermissionsDto.class)
                         .withUserId(USER_ID)
                         .withDomainId(invite.getDomainId())
                         .withInstanceId(invite.getInstanceId())
                         .withActions(invite.getActions());
    }
}
