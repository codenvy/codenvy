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

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents user's permissions to access to some resources
 *
 * @author Sergii Leschenko
 */
@MappedSuperclass
public abstract class AbstractPermissions implements Permissions {

    @Id
    @GeneratedValue
    @Column(name = "id")
    protected String id;

    @Column(name = "userid")
    protected String userId;

    @OneToOne
    @JoinColumn(name = "userid", insertable = false, updatable = false)
    private UserImpl user;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "actions")
    protected List<String> actions;

    @Transient
    private String userIdHolder;

    public AbstractPermissions() {
    }

    public AbstractPermissions(Permissions permissions) {
        this(permissions.getUserId(), permissions.getActions());
    }

    public AbstractPermissions(String userId, List<String> actions) {
        this.userIdHolder = userId;
        this.userId = userId;
        if (actions != null) {
            this.actions = new ArrayList<>(actions);
        }
    }

    /**
     * Returns used id
     */
    @Override
    public String getUserId() {
        return userIdHolder;
    }

    public void setUserId(String userId) {
        this.userIdHolder = userId;
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

    @PreUpdate
    @PrePersist
    private void prePersist() {
        if ("*".equals(userIdHolder)) {
            userId = null;
        } else {
            userId = userIdHolder;
        }
    }

    @PostLoad
    private void postLoad() {
        if (userId == null) {
            userIdHolder = "*";
        } else {
            userIdHolder = userId;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AbstractPermissions)) return false;
        final AbstractPermissions other = (AbstractPermissions)obj;
        return Objects.equals(id, other.id) &&
               Objects.equals(getUserId(), other.getUserId()) &&
               Objects.equals(getInstanceId(), other.getInstanceId()) &&
               Objects.equals(getDomainId(), other.getDomainId()) &&
               Objects.equals(getActions(), other.getActions());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(getUserId());
        hash = 31 * hash + Objects.hashCode(getInstanceId());
        hash = 31 * hash + Objects.hashCode(getDomainId());
        hash = 31 * hash + Objects.hashCode(getActions());
        return hash;
    }

    @Override
    public String toString() {
        return "AbstractPermissions{" +
               "id='" + id + '\'' +
               ", user=" + user +
               ", actions=" + actions +
               '}';
    }
}
