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
package com.codenvy.api.dao.authentication;

import java.util.Set;

/**
 * Manager to persist access tickets for SSO login process
 * <p/>
 *
 * @author Andrey Parfonov
 * @author Sergey Kabashniuk
 */
public interface TicketManager {

    /**
     * Add access ticket
     *
     * @param accessTicket
     *         ticket to add
     */
    void putAccessTicket(AccessTicket accessTicket);

    /**
     * Get access ticket from manager by its token
     *
     * @param accessToken
     *         unique token of access ticket
     * @return access ticket
     */
    AccessTicket getAccessTicket(String accessToken);

    /**
     * Remove access ticket from manager.
     *
     * @param accessToken
     *         unique token of ticket to remove
     * @return removed instance of <code>AccessTicket</code>
     */
    AccessTicket removeTicket(String accessToken);

    /**
     * Get all access tickets.
     *
     * @return set of access tickets
     */
    Set<AccessTicket> getAccessTickets();
}
