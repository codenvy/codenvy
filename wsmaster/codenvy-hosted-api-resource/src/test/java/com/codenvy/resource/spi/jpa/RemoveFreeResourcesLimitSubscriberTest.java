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
package com.codenvy.resource.spi.jpa;

import com.codenvy.resource.spi.FreeResourcesLimitDao;
import com.codenvy.resource.spi.jpa.JpaFreeResourcesLimitDao.RemoveFreeResourcesLimitSubscriber;

import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.notification.EventService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link RemoveFreeResourcesLimitSubscriber}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RemoveFreeResourcesLimitSubscriberTest {
    @Mock
    private EventService          eventService;
    @Mock
    private FreeResourcesLimitDao limitDao;

    @InjectMocks
    RemoveFreeResourcesLimitSubscriber subscriber;

    @Test
    public void shouldSubscribeItself() {
        subscriber.subscribe();

        verify(eventService).subscribe(eq(subscriber));
    }

    @Test
    public void shouldUnsubscribeItself() {
        subscriber.unsubscribe();

        verify(eventService).unsubscribe(eq(subscriber));
    }

    @Test
    public void shouldRemoveMembersOnBeforeOrganizationRemovedEvent() throws Exception {
        final AccountImpl account = new AccountImpl("id", "name", "test");

        subscriber.onEvent(new BeforeAccountRemovedEvent(account));

        verify(limitDao).remove("id");
    }
}
