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
package com.codenvy.api.user.server;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import static java.util.Collections.emptyList;

/**
 * Creates Codenvy admin user.
 *
 * @author Anton Korneta
 */
@Singleton
public class AdminUserCreator {
    private static final Logger LOG = LoggerFactory.getLogger(AdminUserCreator.class);

    @Inject
    private UserManager userManager;

    @Inject
    @SuppressWarnings("unused")
    // this work around needed for Guice to help initialize components in right sequence,
    // because instance of JpaInitializer should be created before components that dependent on dao (such as UserManager)
    private JpaInitializer jpaInitializer;

    @Inject
    @Named("codenvy.admin.name")
    private String name;

    @Inject
    @Named("codenvy.admin.initial_password")
    private String password;

    @Inject
    @Named("codenvy.admin.email")
    private String email;

    @PostConstruct
    public void create() throws ServerException {
        try {
            userManager.getById(name);
        } catch (NotFoundException ex) {
            try {
                userManager.create(new UserImpl(name, password, name, email, emptyList()), false);
                LOG.info("Admin user '" + name + "' successfully created");
            } catch (ConflictException cfEx) {
                LOG.warn("Admin user creation failed", cfEx.getLocalizedMessage());
            }
        }
    }
}
