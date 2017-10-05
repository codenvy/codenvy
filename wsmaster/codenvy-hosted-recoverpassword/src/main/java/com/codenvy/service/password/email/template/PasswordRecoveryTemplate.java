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
package com.codenvy.service.password.email.template;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.mail.template.Template;

/**
 * Template for password recovery email notifications.
 *
 * @author Anton Korneta
 */
public class PasswordRecoveryTemplate extends Template {

  private static final String PASSWORD_RECOVERY_EMAIL_TEMPLATE =
      "/email-templates/password_recovery";

  public PasswordRecoveryTemplate(String tokenAgeMessage, String masterEndpoint, String uuid) {
    super(
        PASSWORD_RECOVERY_EMAIL_TEMPLATE,
        ImmutableMap.of(
            "tokenAgeMessage", tokenAgeMessage,
            "masterEndpoint", masterEndpoint,
            "uuid", uuid));
  }
}
