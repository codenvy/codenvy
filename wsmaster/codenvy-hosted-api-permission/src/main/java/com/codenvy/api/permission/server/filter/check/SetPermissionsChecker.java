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

import com.codenvy.api.permission.shared.model.Permissions;
import org.eclipse.che.api.core.ForbiddenException;

/**
 * Defines contract for domain specific checks, before set permissions.
 *
 * @author Anton Korneta
 */
public interface SetPermissionsChecker {

  /**
   * Checks if the current user is allowed to set permissions.
   *
   * @param permissions permission to set
   * @throws ForbiddenException when it is not allowed to set {@code permissions}
   */
  void check(Permissions permissions) throws ForbiddenException;
}
