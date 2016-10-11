/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.organization.spi.jpa;

import com.codenvy.organization.api.event.BeforeOrganizationRemovedEvent;
import com.codenvy.organization.spi.MemberDao;
import com.codenvy.organization.spi.impl.MemberImpl;
import com.codenvy.organization.spi.impl.OrganizationImpl;

import org.eclipse.che.api.core.notification.EventService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.codenvy.organization.spi.jpa.JpaMemberDao.RemoveMembersBeforeOrganizationRemovedEventSubscriber;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RemoveMembersBeforeOrganizationRemovedEventSubscriber}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RemoveMembersBeforeOrganizationRemovedEventSubscriberTest {
    @Mock
    private EventService eventService;
    @Mock
    private MemberDao    memberDao;

    @InjectMocks
    RemoveMembersBeforeOrganizationRemovedEventSubscriber subscriber;

    @Test
    public void shouldSubscribeItself() {
        subscriber.subscribe();

        verify(eventService).subscribe(eq(subscriber), eq(BeforeOrganizationRemovedEvent.class));
    }

    @Test
    public void shouldUnsubscribeItself() {
        subscriber.unsubscribe();

        verify(eventService).unsubscribe(eq(subscriber), eq(BeforeOrganizationRemovedEvent.class));
    }

    @Test
    public void shouldRemoveMembersOnBeforeOrganizationRemovedEvent() throws Exception {
        final OrganizationImpl organization = new OrganizationImpl("org123", "test-organization", null);

        final MemberImpl member1 = new MemberImpl("user123", "org123", Collections.emptyList());
        final MemberImpl member2 = new MemberImpl("user321", "org123", Collections.emptyList());
        when(memberDao.getMembers(eq("org123"))).thenReturn(Arrays.asList(member1, member2));

        subscriber.onEvent(new BeforeOrganizationRemovedEvent(organization));

        verify(memberDao).getMembers(eq("org123"));
        verify(memberDao).remove(eq("user123"), eq("org123"));
        verify(memberDao).remove(eq("user321"), eq("org123"));
    }
}
