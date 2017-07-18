/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.organization.api.listener.templates;

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 *
 * Defines thymeleaf template organization member added notifications.
 *
 * @author Anton Korneta
 */
public class MemberAddedTemplate extends ThymeleafTemplate {

    public String getPath() {
        return "/email-templates/user_added_to_organization";
    }

    public MemberAddedTemplate(String organizationName,
                               String dashboardEndpoint,
                               String orgQualifiedName,
                               String initiator) {
        context.setVariable("organizationName", organizationName);
        context.setVariable("dashboardEndpoint", dashboardEndpoint);
        context.setVariable("orgQualifiedName", orgQualifiedName);
        context.setVariable("initiator", initiator);
    }

}
