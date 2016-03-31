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
package com.codenvy.service.machine.token.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Machine token DTO.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 *
 */
@DTO
public interface MachineTokenDto {

    String getUserId();

    void setUserId(String userId);

    MachineTokenDto withUserId(String userId);


    String getMachineToken();

    void setMachineToken(String machineToken);

    MachineTokenDto withMachineToken(String machineToken);
}
