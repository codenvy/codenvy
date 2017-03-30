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
package com.codenvy.shared.invite.dto;

import com.codenvy.shared.invite.model.Invite;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface InviteDto extends Invite {
    @Override
    String getEmail();

    void setEmail(String email);

    InviteDto withEmail(String email);

    @Override
    String getInstanceId();

    void setInstanceId(String instanceId);

    InviteDto withInstanceId(String instanceId);

    @Override
    String getDomainId();

    void setDomainId(String domainId);

    InviteDto withDomainId(String domainId);

    @Override
    List<String> getActions();

    void setActions(List<String> actions);

    InviteDto withActions(List<String> actions);
}
