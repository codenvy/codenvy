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

import com.codenvy.api.permission.server.SystemDomain;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;

/**
 * @author Max Shaposhnik
 */
@Entity(name = "SystemPermissions")
@NamedQueries(
        {
                @NamedQuery(name = "SystemPermissions.getByUserId",
                            query = "SELECT permissions " +
                                    "FROM SystemPermissions permissions " +
                                    "WHERE permissions.userId = :userId ")
        }
)
@Table(indexes = @Index(columnList = "userId", unique = true))
@Singleton
public class SystemPermissionsImpl extends AbstractPermissions {

    public SystemPermissionsImpl() {
    }

    public SystemPermissionsImpl(String userId, List<String> actions) {
        super(userId, actions);
    }

    @Override
    public String getInstanceId() {
        return null;
    }

    @Override
    public String getDomainId() {
        return SystemDomain.DOMAIN_ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SystemPermissionsImpl)) return false;
        final SystemPermissionsImpl other = (SystemPermissionsImpl)obj;
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
        return "SystemPermissions{" +
               "user='" + userId + '\'' +
               ", actions=" + actions +
               '}';
    }
}
