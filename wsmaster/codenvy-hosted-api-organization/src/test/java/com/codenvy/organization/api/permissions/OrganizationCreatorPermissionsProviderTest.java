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
package com.codenvy.organization.api.permissions;

import com.codenvy.organization.api.event.PostOrganizationPersistedEvent;
import com.codenvy.organization.spi.MemberDao;
import com.codenvy.organization.spi.impl.MemberImpl;
import com.codenvy.organization.spi.impl.OrganizationImpl;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link OrganizationCreatorPermissionsProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationCreatorPermissionsProviderTest {
    @Mock
    MemberDao    memberDao;
    @Mock
    EventService eventService;

    @InjectMocks
    OrganizationCreatorPermissionsProvider permissionsProvider;

    @Test
    public void shouldSelfSubscribe() {
        permissionsProvider.subscribe();

        verify(eventService).subscribe(eq(permissionsProvider));
    }

    @Test
    public void shouldSelfUnsubscribe() {
        permissionsProvider.unsubscribe();

        verify(eventService).unsubscribe(eq(permissionsProvider));
    }

    @Test
    public void shouldStoreMemberWithAllAllowedActionsOnEvent() throws Exception {
        EnvironmentContext.getCurrent().setSubject(new SubjectImpl(null, "userId", null, false));

        permissionsProvider.onEvent(new PostOrganizationPersistedEvent(new OrganizationImpl("organizationId", "name", "parent")));

        verify(memberDao).store(eq(new MemberImpl("userId",
                                                  "organizationId",
                                                  OrganizationDomain.getActions())));
    }
}
