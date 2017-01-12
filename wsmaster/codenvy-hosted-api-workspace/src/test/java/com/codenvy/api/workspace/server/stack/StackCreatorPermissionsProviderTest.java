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
package com.codenvy.api.workspace.server.stack;

import com.codenvy.api.permission.server.PermissionsManager;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.event.StackPersistedEvent;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 * Tests {@link StackCreatorPermissionsProvider}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class StackCreatorPermissionsProviderTest {

    @Mock
    private EventService eventService;

    @Mock
    private PermissionsManager permManager;

    @InjectMocks
    private StackCreatorPermissionsProvider permProvider;

    @AfterMethod
    public void resetContext() {
        EnvironmentContext.reset();
    }

    @Test
    public void shouldAddPermissions() throws Exception {
        final EnvironmentContext ctx = new EnvironmentContext();
        ctx.setSubject(new SubjectImpl("test-user-name", "test-user-id", "test-token", false));
        EnvironmentContext.setCurrent(ctx);
        final StackImpl stack = createStack();

        permProvider.onEvent(new StackPersistedEvent(stack));

        final ArgumentCaptor<StackPermissionsImpl> captor = ArgumentCaptor.forClass(StackPermissionsImpl.class);
        verify(permManager).storePermission(captor.capture());
        final StackPermissionsImpl perm = captor.getValue();
        assertEquals(perm.getInstanceId(), stack.getId());
        assertEquals(perm.getUserId(), "test-user-id");
        assertEquals(perm.getDomainId(), StackDomain.DOMAIN_ID);
        assertEquals(perm.getActions(), StackDomain.getActions());
    }

    @Test
    public void shouldNotAddPermissionsIfThereIsNoUserInEnvironmentContext() throws Exception {
        permProvider.onEvent(new StackPersistedEvent(createStack()));

        verify(permManager, never()).storePermission(any());
    }

    @Test
    public void shouldSubscribe() {
        permProvider.subscribe();

        verify(eventService).subscribe(permProvider, StackPersistedEvent.class);
    }

    @Test
    public void shouldUnsubscribe() {
        permProvider.unsubscribe();

        verify(eventService).unsubscribe(permProvider, StackPersistedEvent.class);
    }

    private static StackImpl createStack() {
        return StackImpl.builder()
                        .setId("test")
                        .setName("test")
                        .setCreator("test")
                        .build();
    }
}
