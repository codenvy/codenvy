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

import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;

import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class InMemoryTicketManagerTest {
    private final static String TOKEN = "123456789";
    Subject subjectPrincipal = new SubjectImpl("user", "123", "t1", Collections.<String>emptyList(), false);


    @Test
    public void shouldBeAbleToPutAndGetTicket() {
        TicketManager ticketManager = new InMemoryTicketManager();

        assertNull(ticketManager.getAccessTicket(TOKEN));

        AccessTicket expectedTicket = new AccessTicket(TOKEN, subjectPrincipal, "default");

        ticketManager.putAccessTicket(expectedTicket);

        AccessTicket actualTicket = ticketManager.getAccessTicket(TOKEN);

        assertEquals(actualTicket, expectedTicket);
    }

    @Test
    public void shouldBeAbleToGetTickets() {
        TicketManager ticketManager = new InMemoryTicketManager();

        assertEquals(ticketManager.getAccessTickets().size(), 0);

        AccessTicket ticket = new AccessTicket(TOKEN, subjectPrincipal, "default");

        ticketManager.putAccessTicket(ticket);

        assertEquals(ticketManager.getAccessTickets(), Collections.singleton(ticket));
    }

    @Test
    public void shouldBeAbleToRemoveTickets() {

        TicketManager ticketManager = new InMemoryTicketManager();

        ticketManager.putAccessTicket(new AccessTicket(TOKEN, subjectPrincipal, "default"));

        assertEquals(ticketManager.getAccessTickets().size(), 1);

        ticketManager.removeTicket(TOKEN);

        assertEquals(ticketManager.getAccessTickets().size(), 0);
    }
}
