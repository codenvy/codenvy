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
package com.codenvy.auth.sso.server.ticket;

import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.dao.authentication.TicketManager;
import com.codenvy.api.event.user.RemoveUserEvent;

import org.eclipse.che.api.core.notification.EventService;

import org.eclipse.che.api.core.notification.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Alexander Garagatyi
 */
@Singleton
public class LogoutOnUserRemoveSubscriber {
    private static final Logger LOG = LoggerFactory.getLogger(LogoutOnUserRemoveSubscriber.class);

    @Inject
    private EventService eventService;

    @Inject
    private TicketManager ticketManager;

    @PostConstruct
    public void start() {
        eventService.subscribe(new EventSubscriber<RemoveUserEvent>() {
            @Override
            public void onEvent(RemoveUserEvent event) {
                if (null != event && null != event.getUserId()) {
                    String id = event.getUserId();
                    for (AccessTicket accessTicket : ticketManager.getAccessTickets()) {
                        if (id.equals(accessTicket.getPrincipal().getUserId())) {
                            ticketManager.removeTicket(accessTicket.getAccessToken());
                        }
                    }
                }
            }
        });
    }
}
