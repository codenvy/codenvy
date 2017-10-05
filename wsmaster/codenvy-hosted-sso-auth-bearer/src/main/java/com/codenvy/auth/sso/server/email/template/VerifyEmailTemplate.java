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
package com.codenvy.auth.sso.server.email.template;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.mail.template.Template;

/**
 * Thymeleaf template for notifications about verifying email.
 *
 * @author Anton Korneta
 */
public class VerifyEmailTemplate extends Template {

  private static final String VERIFY_EMAIL_ADDRESS_EMAIL_TEMPLATE =
      "/email-templates/verify_email_address";

  public VerifyEmailTemplate(
      String bearerToken, String additionalQueryParameters, String masterEndpoint) {
    super(
        VERIFY_EMAIL_ADDRESS_EMAIL_TEMPLATE,
        ImmutableMap.of(
            "bearertoken",
            bearerToken,
            "additionalQueryParameters",
            additionalQueryParameters,
            "masterEndpoint",
            masterEndpoint));
  }
}
