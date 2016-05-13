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



import com.codahale.metrics.annotation.Gauge;
import org.eclipse.che.commons.lang.IoUtil;

import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.dao.authentication.TicketManager;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Simple implementation of <code>TicketManager</code> */
@Singleton
public class InMemoryTicketManager implements TicketManager {
    private static final Logger                    LOG           = LoggerFactory.getLogger(InMemoryTicketManager.class);
    private final        Map<String, AccessTicket> accessTickets = new HashMap<>();
    private final        ReadWriteLock             readWriteLock = new ReentrantReadWriteLock();


    /** @see TicketManager#putAccessTicket(com.codenvy.api.dao.authentication.AccessTicket) */
    @Override
    public void putAccessTicket(AccessTicket accessTicket) {
        if (accessTicket.getPrincipal() == null || accessTicket.getPrincipal().getUserName() == null) {
            throw new IllegalArgumentException("Access ticket has no principal or username in principal");
        }
        readWriteLock.writeLock().lock();
        try {
            accessTickets.put(accessTicket.getAccessToken(), accessTicket);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /** @see TicketManager#getAccessTicket(java.lang.String) */
    @Override
    public AccessTicket getAccessTicket(String accessToken) {
        if (accessToken == null) {
            return null;
        }

        readWriteLock.readLock().lock();
        try {
            return accessTickets.get(accessToken);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /** @see TicketManager#removeTicket(java.lang.String) */
    @Override
    public AccessTicket removeTicket(String accessToken) {
        if (accessToken == null) {
            return null;
        }

        readWriteLock.writeLock().lock();
        try {

            AccessTicket ticket = accessTickets.remove(accessToken);
            if (ticket == null)
                return null;
            for (String ssoClient : ticket.getRegisteredClients()) {
                // NOTE : must send as many logout request as possible.
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection)new URL(ssoClient + "/_sso/client/logout").openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setInstanceFollowRedirects(false);
                    conn.setConnectTimeout(5 * 1000);
                    conn.setReadTimeout(5 * 1000);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    OutputStream out = conn.getOutputStream();
                    out.write(("authToken=" + URLEncoder.encode(ticket.getAccessToken(), "UTF-8")).getBytes());

                    int responseCode = conn.getResponseCode();
                    LOG.debug("Sent logout request to {} response {}", conn.getURL(), responseCode);
                    if (responseCode / 100 != 2) {
                        if (responseCode == HttpServletResponse.SC_BAD_GATEWAY) {
                            LOG.warn("Tenant {} is unavailable. Logout request not executed", ssoClient);
                        } else if (responseCode == HttpServletResponse.SC_MOVED_TEMPORARILY) {
                            LOG.warn("Logout request for tenant {} was redirected to {}. Logout was omitted",
                                     ssoClient, conn.getHeaderField("Location"));
                        } else {
                            InputStream errorStream = conn.getErrorStream();
                            String message = errorStream != null ? IoUtil.readAndCloseQuietly(errorStream) : "";
                            throw new IOException(
                                    "Unexpected response code '" + responseCode + "' for SSO logout request to" +
                                    " '" + conn.getURL() + "'. " + message);
                        }
                    }

                } catch (IOException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }

                }
            }
            return ticket;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /** @return number of access tickets. */
    @Gauge(name = "auth.sso.access_ticket_number")
    public int size() {
        return accessTickets.size();
    }

    @Override
    public Set<AccessTicket> getAccessTickets() {
        readWriteLock.readLock().lock();
        try {
            return new HashSet<>(accessTickets.values());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
