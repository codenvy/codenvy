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
package com.codenvy.organization.api.listener.templates;

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 * Defines thymeleaf template for organization member removed notifications.
 *
 * @author Anton Korneta
 */
public class MemberRemovedTemplate extends ThymeleafTemplate {

    public MemberRemovedTemplate(String teamName, String initiator) {
        context.setVariable("teamName", teamName);
        context.setVariable("initiator", initiator);
    }

    @Override
    public String getPath() {
        return "/email-templates/user_removed_from_team";
    }

}
