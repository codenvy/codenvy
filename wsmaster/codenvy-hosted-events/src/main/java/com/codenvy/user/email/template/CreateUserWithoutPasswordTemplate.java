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
package com.codenvy.user.email.template;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.mail.template.Template;

/**
 * Thymeleaf template for user creation from bearer token.
 *
 * @author Anton Korneta
 */
public class CreateUserWithoutPasswordTemplate extends Template {

  private static final String USER_CREATED_WITHOUT_PASSWORD_TEMPLATE =
      "/email-templates/user_created_without_password";

  public CreateUserWithoutPasswordTemplate(String masterEndpoint, String userName) {
    super(
        USER_CREATED_WITHOUT_PASSWORD_TEMPLATE,
        ImmutableMap.of("masterEndpoint", masterEndpoint, "userName", userName));
  }
}
