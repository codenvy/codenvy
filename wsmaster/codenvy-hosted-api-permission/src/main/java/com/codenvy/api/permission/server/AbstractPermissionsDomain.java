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
package com.codenvy.api.permission.server;

import com.codenvy.api.permission.shared.model.PermissionsDomain;
import com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract implementation for {@link PermissionsDomain}
 *
 * Note: It supports "setPermission" by default
 *
 * @author Sergii Leschenko
 */
public abstract class AbstractPermissionsDomain implements PermissionsDomain {
    public static final String SET_PERMISSIONS  = "setPermissions";

    private final String       id;
    private final List<String> allowedActions;
    private final boolean      requiresInstance;

    protected AbstractPermissionsDomain(String id, List<String> allowedActions) {
        this(id, allowedActions, true);
    }

    protected AbstractPermissionsDomain(String id, List<String> allowedActions, boolean requiresInstance) {
        this.id = id;
        Set<String> resultActions = new HashSet<>(allowedActions);
        resultActions.add(SET_PERMISSIONS);
        this.allowedActions = ImmutableList.copyOf(resultActions);
        this.requiresInstance = requiresInstance;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<String> getAllowedActions() {
        return allowedActions;
    }

    @Override
    public Boolean isInstanceRequired() {
        return requiresInstance;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AbstractPermissionsDomain)) return false;
        final AbstractPermissionsDomain other = (AbstractPermissionsDomain)obj;
        return Objects.equals(id, other.id) &&
               Objects.equals(allowedActions, other.allowedActions);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(allowedActions);
        return hash;
    }

    @Override
    public String toString() {
        return "PermissionsDomain{" +
               "id='" + id + '\'' +
               ", allowedActions=" + allowedActions +
               "}";
    }
}
