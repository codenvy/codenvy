/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.api.user.server;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.PermissionsManager;
import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.ldap.auth.LdapAuthenticationHandler;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.event.PostUserPersistedEvent;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.core.db.DBInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

/**
 * Creates Codenvy admin user.
 *
 * @author Anton Korneta
 */
@Singleton
public class AdminUserCreator implements EventSubscriber<PostUserPersistedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(AdminUserCreator.class);

    @Inject
    private UserManager userManager;

    @Inject
    PermissionsManager permissionsManager;

    @Inject
    EventService eventService;

    @Inject
    @SuppressWarnings("unused")
    private DBInitializer dbInitializer;

    @Inject
    @Named("codenvy.admin.name")
    private String name;

    @Inject
    @Named("codenvy.admin.initial_password")
    private String password;

    @Inject
    @Named("codenvy.admin.email")
    private String email;

    @Inject
    @Named("sys.auth.handler.default")
    private String authHandler;

    @PostConstruct
    public void create() throws ServerException {
        boolean shouldCreateAdmin = true;
        if (LdapAuthenticationHandler.TYPE.equals(authHandler)) {
            eventService.subscribe(this);
            shouldCreateAdmin = false;
        }
        try {
            User adminUser = userManager.getByName(name);
            grantSystemPermissions(adminUser.getId());
        } catch (NotFoundException ex) {
            if (shouldCreateAdmin) {
                try {
                    User adminUser = userManager.create(new UserImpl(name, email, name, password, emptyList()), false);
                    grantSystemPermissions(adminUser.getId());
                    LOG.info("Admin user '" + name + "' successfully created");
                } catch (ConflictException cfEx) {
                    LOG.warn("Admin user creation failed", cfEx.getLocalizedMessage());
                }
            }
        }
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(PostUserPersistedEvent event) {
        if (event.getUser().getName().equals(name)) {
            grantSystemPermissions(event.getUser().getId());
        }
    }

    private void grantSystemPermissions(String userId) {
        // Add all possible system permissions
        try {
            AbstractPermissionsDomain<? extends AbstractPermissions> systemDomain = permissionsManager.getDomain(SystemDomain.DOMAIN_ID);
            permissionsManager.storePermission(systemDomain.newInstance(userId, null, systemDomain.getAllowedActions()));
        } catch (ServerException | NotFoundException | ConflictException e) {
            LOG.warn(format("System permissions creation failed for user %s", userId), e.getLocalizedMessage());
        }

    }
}
