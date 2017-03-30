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
package com.codenvy.api.invite;

import com.codenvy.api.invite.event.InviteCreatedEvent;
import com.codenvy.api.permission.server.PermissionsManager;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.shared.invite.dto.InviteDto;
import com.codenvy.shared.invite.model.Invite;
import com.codenvy.spi.invite.InviteDao;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Test for {@link InviteManager}.
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class InviteManagerTest {
    @Mock
    private PermissionsManager permissionsManager;
    @Mock
    private UserManager        userManager;
    @Mock
    private InviteDao          inviteDao;
    @Mock
    private EventService       eventService;
    @Mock
    private Page<InviteImpl>   invitesPage;

    @InjectMocks
    private InviteManager inviteManager;

    @Test(dataProvider = "supportedDomains")
    public void shouldStoreInvite(String domain) throws Exception {
        when(userManager.getByEmail(anyString())).thenThrow(new NotFoundException(""));
        when(inviteDao.store(any())).thenReturn(Optional.empty());
        InviteDto toStore = DtoFactory.newDto(InviteDto.class)
                                      .withDomainId(domain)
                                      .withInstanceId("instance123")
                                      .withActions(asList("read", "update"))
                                      .withEmail("user@test.com");

        inviteManager.store(toStore);

        verify(permissionsManager).checkActionsSupporting(domain, asList("read", "update"));
        verify(userManager).getByEmail("user@test.com");
        verify(inviteDao).store(new InviteImpl(toStore));
        verify(eventService).publish(any(InviteCreatedEvent.class));
    }

    @Test(dataProvider = "supportedDomains")
    public void shouldUpdateInvite(String domain) throws Exception {
        when(userManager.getByEmail(anyString())).thenThrow(new NotFoundException(""));
        when(inviteDao.store(any())).thenReturn(Optional.of(new InviteImpl()));
        InviteDto toStore = DtoFactory.newDto(InviteDto.class)
                                      .withDomainId(domain)
                                      .withInstanceId("instance123")
                                      .withActions(asList("read", "update"))
                                      .withEmail("user@test.com");

        inviteManager.store(toStore);

        verify(permissionsManager).checkActionsSupporting(domain, asList("read", "update"));
        verify(userManager).getByEmail("user@test.com");
        verify(inviteDao).store(new InviteImpl(toStore));
        verify(eventService, never()).publish(any());
    }

    @Test(expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "Required non-null invite")
    public void shouldThrowNPEOnStoringNullInvite() throws Exception {
        inviteManager.store(null);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Invitations for specified domain are not supported")
    public void shouldThrowConflictExceptionOnStoringInviteWithUnsupportedDomain() throws Exception {
        inviteManager.store(DtoFactory.newDto(InviteDto.class)
                                      .withDomainId("unsupported"));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "User with specified id is already registered")
    public void shouldThrowConflictExceptionOnInvitingUserWhoIsAlreadyRegistered() throws Exception {
        inviteManager.store(DtoFactory.newDto(InviteDto.class)
                                      .withDomainId(OrganizationDomain.DOMAIN_ID)
                                      .withEmail("user@test.com"));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Specified actions are not supported")
    public void shouldThrowConflictExceptionOnStoringInviteWithNonSupportedActions() throws Exception {
        doThrow(new ConflictException("Specified actions are not supported"))
                .when(permissionsManager).checkActionsSupporting(any(), anyList());

        inviteManager.store(DtoFactory.newDto(InviteDto.class)
                                      .withDomainId(OrganizationDomain.DOMAIN_ID)
                                      .withActions(singletonList("unsupportedAction")));
    }

    @Test
    public void shouldReturnInvitesByEmail() throws Exception {
        doReturn(invitesPage).when(inviteDao).getInvites(anyString(), anyLong(), anyInt());

        Page<? extends Invite> fetchedPage = inviteManager.getInvites("user@test.com", 5, 10);

        assertEquals(fetchedPage, invitesPage);
        verify(inviteDao).getInvites("user@test.com", 5, 10);
    }

    @Test(expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "Required non-null email")
    public void shouldThrowOnFetchingInvitesByNullEmail() throws Exception {
        inviteManager.getInvites(null, 0, 0);
    }

    @Test
    public void shouldReturnInvitesByInstance() throws Exception {
        doReturn(invitesPage).when(inviteDao).getInvites(anyString(), anyString(), anyLong(), anyInt());

        Page<? extends Invite> fetchedPage = inviteManager.getInvites("test", "test123", 5, 10);

        assertEquals(fetchedPage, invitesPage);
        verify(inviteDao).getInvites("test", "test123", 5, 10);
    }

    @Test(expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "Required non-null domain id")
    public void shouldThrowOnFetchingInvitesByNullDomain() throws Exception {
        inviteManager.getInvites(null, "test123", 0, 0);
    }

    @Test(expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "Required non-null instance id")
    public void shouldThrowOnFetchingInvitesByNullInstance() throws Exception {
        inviteManager.getInvites("test", null, 0, 0);
    }

    @Test
    public void shouldRemoveInvite() throws Exception {
        inviteManager.remove("test", "test123", "user@test.com");

        verify(inviteDao).remove("test", "test123", "user@test.com");
    }

    @Test(expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "Required non-null domain id")
    public void shouldThrowOnRemovingInviteByNullDomain() throws Exception {
        inviteManager.remove(null, "test123", "user@test.com");
    }

    @Test(expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "Required non-null instance id")
    public void shouldThrowOnRemovingInviteByNullInstance() throws Exception {
        inviteManager.remove("test", null, "user@test.com");
    }

    @Test(expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "Required non-null email")
    public void shouldThrowOnRemovingInviteByNullEmail() throws Exception {
        inviteManager.remove("test", "test123", null);
    }

    @DataProvider(name = "supportedDomains")
    public Object[][] getSupportedDomains() {
        return new Object[][] {
                {OrganizationDomain.DOMAIN_ID},
                {WorkspaceDomain.DOMAIN_ID}
        };
    }
}
