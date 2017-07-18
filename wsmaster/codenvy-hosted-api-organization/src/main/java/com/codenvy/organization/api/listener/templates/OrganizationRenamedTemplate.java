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
 * Defines thymeleaf template for organization renamed notifications.
 *
 * @author Anton Korneta
 */
public class OrganizationRenamedTemplate extends ThymeleafTemplate {

    public OrganizationRenamedTemplate(String oldName, String newName) {
        context.setVariable("orgOldName", oldName);
        context.setVariable("orgNewName", newName);
    }

    @Override
    public String getPath() {
        return "/email-templates/organization_renamed";
    }

}
