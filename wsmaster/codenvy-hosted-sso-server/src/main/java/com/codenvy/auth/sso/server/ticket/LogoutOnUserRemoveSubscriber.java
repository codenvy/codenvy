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
package com.codenvy.auth.sso.server.ticket;

import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.dao.authentication.TicketManager;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.user.server.event.UserRemovedEvent;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Initiate logout on all sso clients in case of user removal.
 * @author Alexander Garagatyi
 */
@Singleton
public class LogoutOnUserRemoveSubscriber {

    @Inject
    EventService eventService;

    @Inject
    TicketManager ticketManager;

    @PostConstruct
    public void start() {
        eventService.subscribe(new EventSubscriber<UserRemovedEvent>() {
            @Override
            public void onEvent(UserRemovedEvent event) {
                if (null != event && null != event.getUserId()) {
                    String id = event.getUserId();

                    for (AccessTicket accessTicket : ticketManager.getAccessTickets()) {
                        if (id.equals(accessTicket.getUserId())) {
                            ticketManager.removeTicket(accessTicket.getAccessToken());
                        }
                    }
                }
            }
        });
    }
}
