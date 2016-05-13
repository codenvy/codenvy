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

import org.eclipse.che.commons.lang.IoUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Invalidate all roles in all clients associated with given userId
 * in given workspace and account.
 *
 * @author Sergii Kabashniuk
 */
public class RolesInvalidator {
    private static final Logger LOG = LoggerFactory.getLogger(RolesInvalidator.class);
    @Inject
    private TicketManager ticketManager;

    public void invalidateRoles(String userId, String workspaceId, String accountId) {
        for (AccessTicket ticket : ticketManager.getAccessTickets()) {


            if (ticket.getPrincipal().getUserId().equals(userId)) {
                for (String registeredClient : ticket.getRegisteredClients()) {


                    HttpURLConnection conn = null;
                    try {
                        conn = (HttpURLConnection)new URL(registeredClient + "/_sso/client/reset").openConnection();
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setInstanceFollowRedirects(false);
                        conn.setConnectTimeout(5 * 1000);
                        conn.setReadTimeout(5 * 1000);
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        OutputStream out = conn.getOutputStream();
                        out.write(("authToken=" + URLEncoder.encode(ticket.getAccessToken(), "UTF-8")).getBytes());
                        if (workspaceId != null && !workspaceId.isEmpty()) {
                            out.write(("&workspaceId=" + URLEncoder.encode(workspaceId, "UTF-8")).getBytes());
                        }
                        if (accountId != null && !accountId.isEmpty()) {
                            out.write(("&accountId=" + URLEncoder.encode(accountId, "UTF-8")).getBytes());
                        }

                        int responseCode = conn.getResponseCode();
                        LOG.debug("Sent reset request to {} response {}", conn.getURL(), responseCode);
                        if (responseCode / 100 != 2) {
                            if (responseCode == HttpServletResponse.SC_BAD_GATEWAY) {
                                LOG.warn("Client {} is unavailable. Logout request not executed", registeredClient);
                            } else if (responseCode == HttpServletResponse.SC_MOVED_TEMPORARILY) {
                                LOG.warn("Reset request for client {} was redirected to {}. Logout was omitted",
                                         registeredClient, conn.getHeaderField("Location"));
                            } else {
                                InputStream errorStream = conn.getErrorStream();
                                String message = errorStream != null ? IoUtil.readAndCloseQuietly(errorStream) : "";
                                throw new IOException(
                                        "Unexpected response code '" + responseCode + "' for SSO reset request to" +
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
            }
        }

    }
}
