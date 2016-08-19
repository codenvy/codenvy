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

import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.shared.model.Permissions;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Domain for storing actions that are used for managing system e.g. user management, configuration properties management.
 *
 * <p>The list of supported actions by system domain can be configured by following lines
 * <pre>
 *   Multibinder<String> binder = Multibinder.newSetBinder(binder(), String.class, Names.named(SystemDomain.SYSTEM_DOMAIN_ACTIONS));
 *   binder.addBinding().toInstance("customAction");
 * <pre/>
 *
 * @author Sergii Leschenko
 */
public class SystemDomain extends AbstractPermissionsDomain<SystemDomain.SystemPermissionsImpl> {
    public static final String SYSTEM_DOMAIN_ACTIONS = "system.domain.actions";
    public static final String DOMAIN_ID             = "system";
    public static final String MANAGE_CODENVY_ACTION = "manageCodenvy";

    @Inject
    public SystemDomain(@Named(SYSTEM_DOMAIN_ACTIONS) Set<String> allowedActions) {
        super(DOMAIN_ID,
              Stream.concat(allowedActions.stream(),
                            Stream.of(MANAGE_CODENVY_ACTION))
                    .collect(Collectors.toList()),
              false);
    }

    @Override
    public SystemPermissionsImpl doCreateEntity(String userId, String instanceId, List<String> allowedActions) {
        return new SystemPermissionsImpl(userId, allowedActions);
    }


    public class SystemPermissionsImpl extends AbstractPermissions {
        public SystemPermissionsImpl() {

        }

        public SystemPermissionsImpl(String userId, List<String> actions) {
            super(userId, DOMAIN_ID, null, actions);
        }

        @Override
        public String getInstanceId() {
            return null;
        }

        @Override
        public String getDomainId() {
            return DOMAIN_ID;
        }
    }
}
