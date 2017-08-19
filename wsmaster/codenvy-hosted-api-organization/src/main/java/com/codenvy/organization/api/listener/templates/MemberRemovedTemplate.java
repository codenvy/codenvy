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
package com.codenvy.organization.api.listener.templates;

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 * Defines thymeleaf template for organization member removed notifications.
 *
 * @author Anton Korneta
 */
public class MemberRemovedTemplate extends ThymeleafTemplate {

  public MemberRemovedTemplate(String organizationName, String initiator) {
    context.setVariable("organizationName", organizationName);
    context.setVariable("initiator", initiator);
  }

  @Override
  public String getPath() {
    return "/email-templates/user_removed_from_organization";
  }
}
