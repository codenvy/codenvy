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
package com.codenvy.auth.sso.client;

import com.codenvy.machine.authentication.server.MachineTokenRegistry;
import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;

/**
 * Invalidates all the sessions related to the certain machine,
 * when the workspace is stopped.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class MachineSessionInvalidator implements EventSubscriber<WorkspaceStatusEvent> {

    private final MachineTokenRegistry tokenRegistry;
    private final SessionStore         sessionStore;
    private final EventService         eventService;

    @Inject
    public MachineSessionInvalidator(MachineTokenRegistry tokenRegistry,
                                     SessionStore sessionStore,
                                     EventService eventService) {
        this.tokenRegistry = tokenRegistry;
        this.sessionStore = sessionStore;
        this.eventService = eventService;
    }

    @Override
    public void onEvent(WorkspaceStatusEvent event) {
        if (WorkspaceStatusEvent.EventType.STOPPED.equals(event.getEventType())) {
            for (String token : tokenRegistry.removeTokens(event.getWorkspaceId()).values()) {
                final HttpSession session = sessionStore.removeSessionByToken(token);
                if (session != null) {
                    session.removeAttribute("principal");
                    session.invalidate();
                }
            }
        }
    }

    @PostConstruct
    @VisibleForTesting
    void subscribe() {
        eventService.subscribe(this);
    }
}
