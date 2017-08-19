/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.api.permission.server.filter.check;

import static com.codenvy.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;

import com.codenvy.api.permission.shared.model.Permissions;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * Common checks while setting permissions.
 *
 * @author Anton Korneta
 */
@Singleton
public class DefaultSetPermissionsChecker implements SetPermissionsChecker {

  @Override
  public void check(Permissions permissions) throws ForbiddenException {
    if (!EnvironmentContext.getCurrent()
        .getSubject()
        .hasPermission(permissions.getDomainId(), permissions.getInstanceId(), SET_PERMISSIONS)) {
      throw new ForbiddenException("User can't edit permissions for this instance");
    }
  }
}
