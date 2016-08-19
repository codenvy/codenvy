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
package com.codenvy.api.permission.server.model.impl;

import com.codenvy.api.permission.shared.model.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents users' permissions to access to some resources
 *
 * @author Sergii Leschenko
 */
public abstract class AbstractPermissions implements Permissions {

    protected String userId;

    protected List<String> actions;

    public AbstractPermissions() {

    }

    public AbstractPermissions(Permissions permissions) {
        this(permissions.getUserId(), permissions.getDomainId(), permissions.getInstanceId(), permissions.getActions());
    }

    public AbstractPermissions(String userId, String domainId, String instanceId, List<String> actions) {
        this.userId = userId;
        this.actions = new ArrayList<>(actions);
    }

    /**
     * Returns used id
     */
    @Override
    public String getUserId() {
        return userId;
    }


    /**
     * Returns instance id
     */
    @Override
    public abstract String getInstanceId();

    /**
     * Returns domain id
     */
    @Override
    public abstract String getDomainId();

    /**
     * List of actions which user can perform for particular instance
     */
    @Override
    public List<String> getActions() {
        return actions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AbstractPermissions)) return false;
        final AbstractPermissions other = (AbstractPermissions)obj;
        return Objects.equals(userId, other.userId) &&
               Objects.equals(actions, other.actions);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(userId);
        hash = 31 * hash + Objects.hashCode(actions);
        return hash;
    }

    @Override
    public String toString() {
        return "Permissions{" +
               "user='" + userId + '\'' +
               ", actions=" + actions +
               '}';
    }
}
