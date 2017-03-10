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
package com.codenvy.api.invite;

import com.codenvy.shared.invite.dto.InviteDto;
import com.codenvy.shared.invite.model.Invite;

import org.eclipse.che.dto.server.DtoFactory;

/**
 * Helps to convert objects related to invite to DTOs.
 *
 * @author Sergii Leschenko
 */
public final class DtoConverter {
    private DtoConverter() {}

    public static InviteDto asDto(Invite invite) {
        return DtoFactory.newDto(InviteDto.class)
                         .withEmail(invite.getEmail())
                         .withDomainId(invite.getDomainId())
                         .withInstanceId(invite.getInstanceId())
                         .withActions(invite.getActions());
    }
}
