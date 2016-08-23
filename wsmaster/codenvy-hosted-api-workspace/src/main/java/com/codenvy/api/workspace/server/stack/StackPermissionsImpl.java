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
package com.codenvy.api.workspace.server.stack;

import com.codenvy.api.permission.server.model.impl.AbstractPermissions;

import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;
import java.util.Objects;


/**
 * @author Max Shaposhnik
 */

@Entity(name = "StackPermissions")
@NamedQueries(
        {
                @NamedQuery(name = "StackPermissions.getByStackId",
                            query = "SELECT stack " +
                                    "FROM StackPermissions stack " +
                                    "WHERE stack.stackId = :stackId "),
                @NamedQuery(name = "StackPermissions.getByUserId",
                            query = "SELECT stack " +
                                    "FROM StackPermissions stack " +
                                    "WHERE stack.userId = :userId "),
                @NamedQuery(name = "StackPermissions.getByUserAndStackId",
                            query = "SELECT stack " +
                                    "FROM StackPermissions stack " +
                                    "WHERE stack.stackId = :stackId " +
                                    "AND stack.userId = :userId")
        }
)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "stackId"}))
public class StackPermissionsImpl extends AbstractPermissions {

    private String stackId;

    @ManyToOne
    @JoinColumn(name = "stackId", insertable = false, updatable = false)
    private StackImpl stack;


    public StackPermissionsImpl() {

    }

    public StackPermissionsImpl(String userId, String instanceId, List<String> allowedActions) {
        super(userId, allowedActions);
        this.stackId = instanceId;
    }

    @Override
    public String getInstanceId() {
        return stackId;
    }

    @Override
    public String getDomainId() {
        return StackDomain.DOMAIN_ID;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StackPermissionsImpl)) return false;
        final StackPermissionsImpl other = (StackPermissionsImpl)obj;
        return Objects.equals(userId, other.userId) &&
               Objects.equals(stackId, other.stackId) &&
               actions.equals(other.actions);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(userId);
        hash = 31 * hash + Objects.hashCode(stackId);
        hash = 31 * hash + actions.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "StackPermissionsImpl{" +
               "userId='" + userId + '\'' +
               ", stackId='" + stackId + '\'' +
               ", actions=" + actions +
               '}';
    }
}
