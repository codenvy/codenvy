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
package com.codenvy.activity.server;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Listeners(value = MockitoTestNGListener.class)
public class WorkspaceActivityManagerTest {
    private static final long EXPIRE_PERIOD = 10L;

    @Mock
    private WorkspaceManager workspaceManager;

    @Captor
    private ArgumentCaptor<EventSubscriber<WorkspaceStatusEvent>> captor;

    @Mock
    EventService eventService;

    private WorkspaceActivityManager activityManager;

    @BeforeMethod
    private void setUp() {
        activityManager = new WorkspaceActivityManager(EXPIRE_PERIOD, workspaceManager, eventService);
    }

    @Test
    public void shouldAddNewActiveWorkspace() throws Exception {
        final String wsId = "testWsId";
        final long expiredTime = 1000L;
        final Map<String, Long> activeWorkspaces = getActiveWorkspaces(activityManager);
        boolean wsAlreadyAdded = activeWorkspaces.containsKey(wsId);

        activityManager.update(wsId, expiredTime);

        assertFalse(wsAlreadyAdded);
        assertFalse(activeWorkspaces.isEmpty());
    }

    @Test
    public void shouldUpdateTheWorkspaceExpirationIfItWasPreviouslyActive() throws Exception {
        final String wsId = "testWsId";
        final long expiredTime = 1000L;
        final long newExpireTime = 2000L;
        final Map<String, Long> activeWorkspaces = getActiveWorkspaces(activityManager);
        boolean wsAlreadyAdded = activeWorkspaces.containsKey(wsId);
        activityManager.update(wsId, expiredTime);


        activityManager.update(wsId, newExpireTime);
        final long workspaceStopTime = activeWorkspaces.get(wsId);

        assertFalse(wsAlreadyAdded);
        assertFalse(activeWorkspaces.isEmpty());
        assertEquals(newExpireTime + EXPIRE_PERIOD, workspaceStopTime);
    }

    @Test
    public void shouldAddWorkspaceForTrackActivityWhenWorkspaceRunning() throws Exception {
        final String wsId = "testWsId";
        activityManager.subscribe();
        verify(eventService).subscribe(captor.capture());
        final EventSubscriber<WorkspaceStatusEvent> subscriber = captor.getValue();

        subscriber.onEvent(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                     .withEventType(WorkspaceStatusEvent.EventType.RUNNING)
                                     .withWorkspaceId(wsId));
        final Map<String, Long> activeWorkspaces = getActiveWorkspaces(activityManager);

        assertTrue(activeWorkspaces.containsKey(wsId));
    }

    @Test
    public void shouldCeaseToTrackTheWorkspaceActivityAfterStopping() throws Exception {
        final String wsId = "testWsId";
        final long expiredTime = 1000L;
        activityManager.update(wsId, expiredTime);
        activityManager.subscribe();
        verify(eventService).subscribe(captor.capture());
        final EventSubscriber<WorkspaceStatusEvent> subscriber = captor.getValue();

        final Map<String, Long> activeWorkspaces = getActiveWorkspaces(activityManager);
        final boolean contains = activeWorkspaces.containsKey(wsId);
        subscriber.onEvent(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                     .withEventType(WorkspaceStatusEvent.EventType.STOPPED)
                                     .withWorkspaceId(wsId));

        assertTrue(contains);
        assertTrue(activeWorkspaces.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> getActiveWorkspaces(WorkspaceActivityManager workspaceActivityManager) throws Exception {
        for (Field field : workspaceActivityManager.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getName().equals("activeWorkspaces")) {
                return (Map<String, Long>)field.get(workspaceActivityManager);
            }
        }
        throw new IllegalAccessException();
    }
}

