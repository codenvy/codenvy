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
package com.codenvy.user.email.template;

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 * Thymeleaf template for user that created by admins.
 *
 * @author Anton Korneta
 */
public class CreateUserWithPasswordTemplate extends ThymeleafTemplate {

    public CreateUserWithPasswordTemplate(String masterEndpoint,
                                          String resetPasswordLink,
                                          String userName) {
        context.setVariable("masterEndpoint", masterEndpoint);
        context.setVariable("resetPasswordLink", resetPasswordLink);
        context.setVariable("userName", userName);
    }

    @Override
    public String getPath() {
        return "/email-templates/user_created_with_password";
    }

}
