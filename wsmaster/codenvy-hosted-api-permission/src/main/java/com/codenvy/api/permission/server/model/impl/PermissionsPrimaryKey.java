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

import java.io.Serializable;
import java.util.Objects;

/**
 * Primary key for {@link PermissionsImpl} entity.
 *
 * @author Max Shaposhnik
 */
public class PermissionsPrimaryKey implements Serializable {

    private String userId;
    private String instanceId;

    public PermissionsPrimaryKey() {

    }

    public PermissionsPrimaryKey(String userId, String instanceId) {
        this.userId = userId;
        this.instanceId = instanceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PermissionsPrimaryKey)) return false;
        PermissionsPrimaryKey that = (PermissionsPrimaryKey)obj;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(instanceId, that.instanceId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(userId);
        hash = 31 * hash + Objects.hashCode(instanceId);
        return hash;
    }
}
