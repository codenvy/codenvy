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

import javax.persistence.AssociationOverride;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.List;

/**
 * System permissions data object.
 *
 * @author Max Shaposhnik
 */
@Entity(name = "SystemPermissions")
@NamedQueries(
        {
                @NamedQuery(name = "SystemPermissions.getByUserId",
                            query = "SELECT permissions " +
                                    "FROM SystemPermissions permissions " +
                                    "WHERE permissions.userId = :userId "),
                @NamedQuery(name = "SystemPermissions.getAll",
                            query = "SELECT permissions " +
                                    "FROM SystemPermissions permissions "),
                @NamedQuery(name = "SystemPermissions.getTotalCount",
                            query = "SELECT COUNT(permissions) " +
                                    "FROM SystemPermissions permissions ")
        }
)
@Table(name = "systempermissions")
public class SystemPermissionsImpl extends AbstractPermissions {

    public SystemPermissionsImpl() {
    }

    public SystemPermissionsImpl(String userId, List<String> actions) {
        super(userId, actions);
    }

    public SystemPermissionsImpl(SystemPermissionsImpl permissions) {
        this(permissions.getUserId(), permissions.getActions());
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
    public String toString() {
        return "SystemPermissions{" +
               "user='" + getUserId() + '\'' +
               ", actions=" + actions +
               '}';
    }
}
