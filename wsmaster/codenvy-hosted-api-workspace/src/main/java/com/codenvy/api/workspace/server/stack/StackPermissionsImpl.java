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

import java.util.List;


/**
 * @author Max Shaposhnik
 */

public class StackPermissionsImpl extends AbstractPermissions {

    private String stackId;

    public StackPermissionsImpl() {

    }

    public StackPermissionsImpl(StackPermissionsImpl stackPermissions) {
        super(stackPermissions.getUserId(), stackPermissions.getActions());
        this.stackId = stackPermissions.getInstanceId();
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
}
