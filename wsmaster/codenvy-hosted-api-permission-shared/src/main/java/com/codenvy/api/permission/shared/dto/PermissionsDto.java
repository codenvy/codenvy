/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.permission.shared.dto;

import com.codenvy.api.permission.shared.model.Permissions;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface PermissionsDto extends Permissions {
    @Override
    String getUserId();

    void setUserId (String userId);

    PermissionsDto withUserId(String userId);

    @Override
    String getDomainId();

    void setDomainId(String domainId);

    PermissionsDto withDomainId(String domainId);

    @Override
    String getInstanceId();

    void setInstanceId(String instanceId);

    PermissionsDto withInstanceId(String instanceId);

    @Override
    List<String> getActions();

    void setActions(List<String> actions);

    PermissionsDto withActions(List<String> actions);
}
