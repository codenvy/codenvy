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
import com.codenvy.api.permission.shared.model.Permissions;

import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.List;


/**
 * Stack permissions data object.
 *
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
                                    "WHERE stack.userId IS NULL OR stack.userId = :userId "),
                @NamedQuery(name = "StackPermissions.getByUserAndStackId",
                            query = "SELECT stack " +
                                    "FROM StackPermissions stack " +
                                    "WHERE stack.stackId = :stackId " +
                                    "AND (stack.userId IS NULL OR stack.userId = :userId) ")
        }
)
@Table(indexes = {@Index(columnList = "userId, stackId", unique = true),
                  @Index(columnList = "stackId")})
public class StackPermissionsImpl extends AbstractPermissions {

    @Column
    private String stackId;

    @ManyToOne
    @JoinColumn(name = "stackId", insertable = false, updatable = false)
    private StackImpl stack;

    public StackPermissionsImpl() {}

    public StackPermissionsImpl(Permissions permissions) {
        this(permissions.getUserId(), permissions.getInstanceId(), permissions.getActions());
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
    public String toString() {
        return "StackPermissionsImpl{" +
               "userId='" + getUserId() + '\'' +
               ", stackId='" + stackId + '\'' +
               ", actions=" + actions +
               '}';
    }
}
