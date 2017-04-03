/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.service.password.email.template;

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 * Thymeleaf template for password recovery email notifications.
 *
 * @author Anton Korneta
 */
public class PasswordRecoveryTemplate extends ThymeleafTemplate {

    public PasswordRecoveryTemplate(String tokenAgeMessage,
                                    String masterEndpoint,
                                    String uuid) {
        context.setVariable("tokenAgeMessage", tokenAgeMessage);
        context.setVariable("masterEndpoint", masterEndpoint);
        context.setVariable("uuid", uuid);
    }

    @Override
    public String getPath() {
        return "/email-templates/password_recovery";
    }
}
