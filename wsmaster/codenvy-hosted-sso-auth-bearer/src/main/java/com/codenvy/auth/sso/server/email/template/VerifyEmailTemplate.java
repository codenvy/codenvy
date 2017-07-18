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
package com.codenvy.auth.sso.server.email.template;

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 * Thymeleaf template for notifications about verifying email.
 *
 * @author Anton Korneta
 */
public class VerifyEmailTemplate extends ThymeleafTemplate {

    public VerifyEmailTemplate(String bearerToken,
                               String additionalQueryParameters,
                               String masterEndpoint) {
        context.setVariable("bearertoken", bearerToken);
        context.setVariable("additionalQueryParameters", additionalQueryParameters);
        context.setVariable("masterEndpoint", masterEndpoint);
    }

    @Override
    public String getPath() {
        return "/email-templates/verify_email_address";
    }
}
