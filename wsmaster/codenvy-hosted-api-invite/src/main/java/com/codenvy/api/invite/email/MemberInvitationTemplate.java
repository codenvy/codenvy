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
package com.codenvy.api.invite.email;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.mail.template.Template;

/**
 * Defines thymeleaf template organization member invitation.
 *
 * @author Sergii Leshchenko
 */
public class MemberInvitationTemplate extends Template {

  private static final String USER_ORGANIZATION_INVITATION_EMAIL_TEMPLATE =
      "/email-templates/user_organization_invitation";

  public MemberInvitationTemplate(String initiator, String joinLink) {
    super(
        USER_ORGANIZATION_INVITATION_EMAIL_TEMPLATE,
        ImmutableMap.of("initiator", initiator, "joinLink", joinLink));
  }
}
