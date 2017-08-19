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

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 * Defines thymeleaf template workspace worker invitation.
 *
 * @author Sergii Leshchenko
 */
public class WorkerInvitationTemplate extends ThymeleafTemplate {
  public WorkerInvitationTemplate(String initiator, String joinLink) {
    context.setVariable("initiator", initiator);
    context.setVariable("joinLink", joinLink);
  }

  @Override
  public String getPath() {
    return "/email-templates/user_workspace_invitation";
  }
}
