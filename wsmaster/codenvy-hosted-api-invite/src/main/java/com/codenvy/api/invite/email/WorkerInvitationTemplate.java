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
package com.codenvy.api.invite.email;

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 * Defines thymeleaf template workspace worker invitation.
 *
 * @author Sergii Leshchenko
 */
public class WorkerInvitationTemplate extends ThymeleafTemplate {
    public WorkerInvitationTemplate(String initiator,
                                    String joinLink) {
        context.setVariable("initiator", initiator);
        context.setVariable("joinLink", joinLink);
    }

    @Override
    public String getPath() {
        return "/email-templates/user_workspace_invitation";
    }

}
