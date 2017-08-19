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

import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.STOPPED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;

import com.codenvy.machine.authentication.server.MachineTokenRegistry;
import javax.servlet.http.HttpSession;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.commons.lang.NameGenerator;
import org.junit.Test;

/** @author Yevhenii Voevodin */
public class MachineSessionInvalidatorTest {

  @Test
  public void subscriberMustExpireAllTheSessionsRelatedToTheWorkspace() throws Exception {
    final MachineTokenRegistry registry = new MachineTokenRegistry();
    final EventService eventService = new EventService();
    final SessionStore sessionStore = new SessionStore();
    final MachineSessionInvalidator invalidator =
        new MachineSessionInvalidator(registry, sessionStore, eventService);
    invalidator.subscribe();
    // generating a few tokens for the workspace
    final String token1 = registry.generateToken("user123", "workspace123");
    final String token2 = registry.generateToken("user234", "workspace123");
    final String token3 = registry.generateToken("user345", "workspace123");
    // creating the sessions for the tokens
    final HttpSession httpSession1 = mockHttpSession();
    sessionStore.saveSession(token1, httpSession1);
    final HttpSession httpSession2 = mockHttpSession();
    sessionStore.saveSession(token2, httpSession2);

    // publishing the event to trigger the subscriber
    eventService.publish(
        newDto(WorkspaceStatusEvent.class).withWorkspaceId("workspace123").withEventType(STOPPED));

    // verifying whether all the sessions were expired
    assertNull(sessionStore.getSession(token1));
    verify(httpSession1).invalidate();
    assertNull(sessionStore.getSession(token2));
    verify(httpSession2).invalidate();
    assertNull(sessionStore.getSession(token3));
  }

  private static HttpSession mockHttpSession() {
    final HttpSession session = mock(HttpSession.class);
    when(session.getId()).thenReturn(NameGenerator.generate("http-session", 10));
    return session;
  }
}
