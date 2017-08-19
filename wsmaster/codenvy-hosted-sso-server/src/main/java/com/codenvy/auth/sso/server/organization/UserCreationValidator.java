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
package com.codenvy.auth.sso.server.organization;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;

/**
 * Check if user email and name is allowed to validate before workspace creation.
 *
 * @author Sergii Kabashniuk
 */
public interface UserCreationValidator {

  /**
   * Ensure user and workspace name is eligible to validate email before workspace creation.
   *
   * @param email - user email. Null or empty value should throw BadRequestException.
   * @param userName - user name. Null or empty value should throw BadRequestException.
   * @throws BadRequestException - if user's email or name have incorrect values
   * @throws ConflictException - if user with given email or name cannot be created
   * @throws ServerException - if other error occurs
   */
  void ensureUserCreationAllowed(String email, String userName)
      throws BadRequestException, ConflictException, ServerException;
}
