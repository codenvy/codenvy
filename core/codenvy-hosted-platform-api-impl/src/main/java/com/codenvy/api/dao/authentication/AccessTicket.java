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
package com.codenvy.api.dao.authentication;


import org.eclipse.che.commons.subject.Subject;

import javax.ws.rs.ForbiddenException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Random alphanumeric sequence that provide access to codenvy services.
 * Can be saved in the cookie in browser or provides some other way (i.e. query, header).
 *
 * @author Andrey Parfonov
 * @author Sergey Kabashniuk
 */
public final class AccessTicket {
    private final Set<String> registeredClients;
    private final Subject     principal;
    private final String      authHandlerType;
    /** Time of ticket creation in milliseconds. */
    private       long        creationTime;
    /** Value of access cookie associated with this access key. */
    private       String      accessToken;

    public AccessTicket(String accessToken, Subject principal, String authHandlerType) {
        this(accessToken, principal, authHandlerType, System.currentTimeMillis());
    }


    public AccessTicket(String accessToken, Subject principal, String authHandlerType, long creationTime) {

        if (accessToken == null) {
            throw new IllegalArgumentException("Invalid access token: " + accessToken);
        }
        if (principal == null) {
            throw new IllegalArgumentException("Invalid principal: " + principal);
        }
        if (authHandlerType == null) {
            throw new IllegalArgumentException("Invalid authHandlerType: " + principal);
        }
        if (creationTime < 0) {
            throw new IllegalArgumentException("Invalid creation time : " + creationTime);
        }
        this.accessToken = accessToken;
        this.authHandlerType = authHandlerType;

        this.principal = principal;
        this.creationTime = creationTime;
        this.registeredClients = new HashSet<>();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Subject getPrincipal() {
        return new Subject() {
            @Override
            public String getUserName() {
                return principal.getUserName();
            }

            @Override
            public boolean isMemberOf(String role) {
                return principal.isMemberOf(role);
            }

            @Override
            public boolean hasPermission(String domain, String instance, String action) {
                return principal.hasPermission(domain, instance, action);
            }

            @Override
            public void checkPermission(String domain, String instance, String action) throws ForbiddenException {
                principal.checkPermission(domain, instance, action);
            }

            @Override
            public String getToken() {
                return accessToken;
            }

            @Override
            public String getUserId() {
                return principal.getUserId();
            }

            @Override
            public boolean isTemporary() {
                return principal.isTemporary();
            }
        };
    }

    /**
     * @return type of authentication handler was used for current user authentication.
     */
    public String getAuthHandlerType() {
        return authHandlerType;
    }


    /** Get time of token creation. */
    public long getCreationTime() {
        return creationTime;
    }

    /** Get copy of the set of registered clients for this token. */
    public Set<String> getRegisteredClients() {
        return new LinkedHashSet<>(registeredClients);
    }

    /**
     * Register SSO client for this token.
     *
     * @param clientUrl
     *         - Indicate that SSO server knows about registration of the current user in given client url.
     */
    public synchronized void registerClientUrl(String clientUrl) {
        registeredClients.add(clientUrl);
    }

    /**
     * Unregister SSO client for this token.
     *
     * @param clientUrl
     *         - given client url to unregister
     */
    public synchronized void unRegisterClientUrl(String clientUrl) {
        registeredClients.remove(clientUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessTicket that = (AccessTicket)o;

        if (creationTime != that.creationTime) return false;
        if (accessToken != null ? !accessToken.equals(that.accessToken) : that.accessToken != null) return false;
        if (authHandlerType != null ? !authHandlerType.equals(that.authHandlerType) : that.authHandlerType != null) return false;
        if (principal != null ? !principal.equals(that.principal) : that.principal != null) return false;
        if (registeredClients != null ? !registeredClients.equals(that.registeredClients) : that.registeredClients != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = registeredClients != null ? registeredClients.hashCode() : 0;
        result = 31 * result + (principal != null ? principal.hashCode() : 0);
        result = 31 * result + (authHandlerType != null ? authHandlerType.hashCode() : 0);
        result = 31 * result + (int)(creationTime ^ (creationTime >>> 32));
        result = 31 * result + (accessToken != null ? accessToken.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccessTicket{");
        sb.append("registeredClients=").append(registeredClients);
        sb.append(", principal=").append(principal);
        sb.append(", authHandlerType='").append(authHandlerType).append('\'');
        sb.append(", creationTime=").append(creationTime);
        sb.append(", accessToken='").append(accessToken).append('\'');
        sb.append('}');
        return sb.toString();
    }
}


