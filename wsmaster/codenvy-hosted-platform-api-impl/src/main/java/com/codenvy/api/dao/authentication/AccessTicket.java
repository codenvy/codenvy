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
    private final String      userId;
    private final String      authHandlerType;
    /** Time of ticket creation in milliseconds. */
    private       long        creationTime;
    /** Value of access cookie associated with this access key. */
    private       String      accessToken;

    public AccessTicket(String accessToken, String userId, String authHandlerType) {
        this(accessToken, userId, authHandlerType, System.currentTimeMillis());
    }


    public AccessTicket(String accessToken, String userId, String authHandlerType, long creationTime) {

        if (accessToken == null) {
            throw new IllegalArgumentException("Invalid access token: " + accessToken);
        }
        if (userId == null) {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }
        if (authHandlerType == null) {
            throw new IllegalArgumentException("Invalid authHandlerType: " + authHandlerType);
        }
        if (creationTime < 0) {
            throw new IllegalArgumentException("Invalid creation time : " + creationTime);
        }
        this.accessToken = accessToken;
        this.authHandlerType = authHandlerType;

        this.userId = userId;
        this.creationTime = creationTime;
        this.registeredClients = new HashSet<>();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserId() {
        return userId;
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
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (registeredClients != null ? !registeredClients.equals(that.registeredClients) : that.registeredClients != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = registeredClients != null ? registeredClients.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (authHandlerType != null ? authHandlerType.hashCode() : 0);
        result = 31 * result + (int)(creationTime ^ (creationTime >>> 32));
        result = 31 * result + (accessToken != null ? accessToken.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccessTicket{");
        sb.append("registeredClients=").append(registeredClients);
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", authHandlerType='").append(authHandlerType).append('\'');
        sb.append(", creationTime=").append(creationTime);
        sb.append(", accessToken='").append(accessToken).append('\'');
        sb.append('}');
        return sb.toString();
    }
}


