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

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents users' permissions to access to some resources
 *
 * @author Sergii Leschenko
 */
@Entity(name = "Permissions")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries(
        {
                @NamedQuery(name = "Permissions.getByDomainAndInstance",
                            query = "SELECT permissions " +
                                    "FROM Permissions permissions " +
                                    "WHERE permissions.instanceId = :instanceId")
        }
)
@IdClass(PermissionsPrimaryKey.class)
public class PermissionsImpl implements Permissions {

    @Id
    protected String       userId;
    @Transient
    protected String       domainId;
    @Id
    protected String       instanceId;
    @ElementCollection
    protected List<String> actions;

    public PermissionsImpl() {

    }

    public PermissionsImpl(Permissions permissions) {
        this(permissions.getUserId(), permissions.getDomainId(), permissions.getInstanceId(), permissions.getActions());
    }

    public PermissionsImpl(String userId, String domainId, String instanceId, List<String> actions) {
        this.userId = userId;
        this.domainId = domainId;
        this.instanceId = instanceId;
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
     * Returns domainId id
     */
    @Override
    public String getDomainId() {
        return domainId;
    }

    /**
     * Returns instance id
     */
    @Override
    public String getInstanceId() {
        return instanceId;
    }

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
        if (!(obj instanceof PermissionsImpl)) return false;
        final PermissionsImpl other = (PermissionsImpl)obj;
        return Objects.equals(userId, other.userId) &&
               Objects.equals(domainId, other.domainId) &&
               Objects.equals(instanceId, other.instanceId) &&
               Objects.equals(actions, other.actions);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(userId);
        hash = 31 * hash + Objects.hashCode(domainId);
        hash = 31 * hash + Objects.hashCode(instanceId);
        hash = 31 * hash + Objects.hashCode(actions);
        return hash;
    }

    @Override
    public String toString() {
        return "Permissions{" +
               "user='" + userId + '\'' +
               ", domainId='" + domainId + '\'' +
               ", instance='" + instanceId + '\'' +
               ", actions=" + actions +
               '}';
    }
}
