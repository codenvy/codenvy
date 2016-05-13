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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Task for removing expired tokens.
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class AccessTicketInvalidator extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(AccessTicketInvalidator.class);
    private final Timer         timer;
    @Inject
    private       TicketManager ticketManager;
    /** Period of time when access ticked keep valid */
    @Named("auth.sso.access_ticket_lifetime_seconds")
    @Inject
    private       int           ticketLifeTimeSeconds;

    public AccessTicketInvalidator() {
        this.timer = new Timer("sso-access-ticket-invalidator", true);
    }

    @Override
    public void run() {
        for (AccessTicket accessTicket : ticketManager.getAccessTickets()) {
            if (System.currentTimeMillis() > accessTicket.getCreationTime() + ticketLifeTimeSeconds * 1000) {
                LOG.info("Initiate user {} sso logout by timeout", accessTicket.getPrincipal().getUserName());
                ticketManager.removeTicket(accessTicket.getAccessToken());
            }
        }
    }

    @PostConstruct
    public void startTimer() {
        //wait 1 second and run each minute
        timer.schedule(this, 1000, 60000);
    }

    @PreDestroy
    public void cancelTimer() {
        timer.cancel();

    }


}
