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

import com.codenvy.api.permission.server.model.impl.PermissionsImpl;

import org.eclipse.che.api.user.server.model.impl.UserImpl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.List;

import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;

/**
 * @author Max Shaposhnik
 */

@Entity(name = "StackPermissions")
@DiscriminatorValue(StackDomain.DOMAIN_ID)
public class StackPermissionsImpl extends PermissionsImpl {

    @OneToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private UserImpl user;

    @OneToOne
    @JoinColumn(name = "instanceId", insertable = false, updatable = false)
    private StackImpl stack;

    public  StackPermissionsImpl() {

    }

    public StackPermissionsImpl(StackPermissionsImpl stackPermissions) {
        super(stackPermissions.getUserId(), StackDomain.DOMAIN_ID, stackPermissions.getInstanceId(), stackPermissions.getActions());
    }

    public StackPermissionsImpl(String userId, String instanceId, List<String> actions) {
        super(userId, StackDomain.DOMAIN_ID, instanceId, actions);
    }
}
