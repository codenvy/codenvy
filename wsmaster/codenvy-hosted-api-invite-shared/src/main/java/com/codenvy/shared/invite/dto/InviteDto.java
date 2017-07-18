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
