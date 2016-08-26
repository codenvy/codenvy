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

import org.eclipse.che.api.user.server.model.impl.UserImpl;

import javax.persistence.ElementCollection;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents users' permissions to access to some resources
 *
 * @author Sergii Leschenko
 */
@MappedSuperclass
public abstract class AbstractPermissions implements Permissions {

    @Id
    @GeneratedValue
    protected String id;

    protected String userId;

    @OneToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private UserImpl user;

    @ElementCollection
    protected List<String> actions;

    public AbstractPermissions() {

    }

    public AbstractPermissions(Permissions permissions) {
        this(permissions.getUserId(), permissions.getActions());
    }

    public AbstractPermissions(String userId, List<String> actions) {
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
        return Objects.equals(getUserId(), other.getUserId()) &&
               Objects.equals(getInstanceId(), other.getInstanceId()) &&
               Objects.equals(getDomainId(), other.getDomainId()) &&
               Objects.equals(getActions(), other.getActions());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(getUserId());
        hash = 31 * hash + Objects.hashCode(getInstanceId());
        hash = 31 * hash + Objects.hashCode(getDomainId());
        hash = 31 * hash + Objects.hashCode(getActions());
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
