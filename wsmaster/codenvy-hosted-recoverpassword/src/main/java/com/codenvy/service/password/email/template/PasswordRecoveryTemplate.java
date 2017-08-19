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

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 * Thymeleaf template for password recovery email notifications.
 *
 * @author Anton Korneta
 */
public class PasswordRecoveryTemplate extends ThymeleafTemplate {

  public PasswordRecoveryTemplate(String tokenAgeMessage, String masterEndpoint, String uuid) {
    context.setVariable("tokenAgeMessage", tokenAgeMessage);
    context.setVariable("masterEndpoint", masterEndpoint);
    context.setVariable("uuid", uuid);
  }

  @Override
  public String getPath() {
    return "/email-templates/password_recovery";
  }
}
