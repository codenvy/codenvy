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
package com.codenvy.auth.sso.client;

import com.codenvy.machine.authentication.server.MachineTokenRegistry;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;

/**
 * Invalidates all the sessions related to the certain machine, when the workspace is stopped.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class MachineSessionInvalidator implements EventSubscriber<WorkspaceStatusEvent> {

  private final MachineTokenRegistry tokenRegistry;
  private final SessionStore sessionStore;
  private final EventService eventService;

  @Inject
  public MachineSessionInvalidator(
      MachineTokenRegistry tokenRegistry, SessionStore sessionStore, EventService eventService) {
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
