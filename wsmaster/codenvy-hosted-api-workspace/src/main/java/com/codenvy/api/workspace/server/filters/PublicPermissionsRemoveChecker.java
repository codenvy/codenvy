/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.workspace.server.filters;

import com.codenvy.api.machine.server.recipe.RecipeDomain;
import com.codenvy.api.permission.server.PermissionsManager;
import com.codenvy.api.permission.server.filter.check.DefaultRemovePermissionsChecker;
import com.codenvy.api.permission.server.filter.check.RemovePermissionsChecker;
import com.codenvy.api.workspace.server.stack.StackDomain;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

import static com.codenvy.api.permission.server.SystemDomain.DOMAIN_ID;
import static com.codenvy.api.permission.server.SystemDomain.MANAGE_SYSTEM_ACTION;

/**
 * Recipe and Stack domains remove permissions checker.
 *
 * @author Anton Korneta
 */
@Singleton
public class PublicPermissionsRemoveChecker implements RemovePermissionsChecker {

    private final DefaultRemovePermissionsChecker defaultChecker;
    private final PermissionsManager              permissionsManager;

    @Inject
    public PublicPermissionsRemoveChecker(DefaultRemovePermissionsChecker defaultChecker,
                                          PermissionsManager permissionsManager) {
        this.defaultChecker = defaultChecker;
        this.permissionsManager = permissionsManager;
    }

    @Override
    public void check(String user, String domain, String instance) throws ForbiddenException {
        if (!"*".equals(user) ||
            !EnvironmentContext.getCurrent().getSubject().hasPermission(DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)) {
            defaultChecker.check(user, domain, instance);
            return;
        }
        final Set<String> actions = new HashSet<>();
        try {
            actions.addAll(permissionsManager.get(user, domain, instance).getActions());
        } catch (ApiException ignored) {
        }

        // perform default check if no search action found or its not admin user
        if (!actions.contains(StackDomain.SEARCH) && !actions.contains(RecipeDomain.SEARCH)) {
            defaultChecker.check(user, domain, instance);
        }
    }

}
