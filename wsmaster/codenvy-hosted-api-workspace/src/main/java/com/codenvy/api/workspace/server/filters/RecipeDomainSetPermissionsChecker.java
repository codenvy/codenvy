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

import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.api.permission.server.filter.check.DefaultSetPermissionsChecker;
import com.codenvy.api.permission.server.filter.check.SetPermissionsChecker;
import com.codenvy.api.permission.shared.model.Permissions;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

import static com.codenvy.api.machine.server.recipe.RecipeDomain.READ;
import static com.codenvy.api.machine.server.recipe.RecipeDomain.SEARCH;
import static com.codenvy.api.machine.server.recipe.RecipeDomain.getActions;
import static com.codenvy.api.permission.server.SystemDomain.*;
import static com.codenvy.api.permission.server.SystemDomain.MANAGE_SYSTEM_ACTION;
import static java.util.stream.Collectors.toList;

/**
 * Recipe domain specific remove permission checker.
 *
 * @author Anton Korneta
 */
@Singleton
public class RecipeDomainSetPermissionsChecker implements SetPermissionsChecker {

    private final DefaultSetPermissionsChecker defaultChecker;

    @Inject
    public RecipeDomainSetPermissionsChecker(DefaultSetPermissionsChecker defaultChecker) {
        this.defaultChecker = defaultChecker;
    }

    @Override
    public void check(Permissions permissions) throws ForbiddenException {
        if (!"*".equals(permissions.getUserId())) {
            defaultChecker.check(permissions);
            return;
        }
        final Set<String> unsupportedPublicActions = new HashSet<>(permissions.getActions());
        unsupportedPublicActions.remove(READ);

        //public search is supported only for admins
        if (EnvironmentContext.getCurrent().getSubject().hasPermission(DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)) {
            unsupportedPublicActions.remove(SEARCH);
        } else {
            defaultChecker.check(permissions);
        }

        if (!unsupportedPublicActions.isEmpty()) {
            throw new ForbiddenException("Following actions are not supported for setting as public:" +
                                         getActions().stream()
                                                     .filter(a -> !(a.equals(READ) || a.equals(SEARCH)))
                                                     .collect(toList()));
        }
    }

}
