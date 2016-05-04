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
package com.codenvy.machine.authentication.ide;

import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;

/**
 * GWT Client for MachineToken Service.
 *
 * @author Anton Korneta
 */
public interface MachineTokenServiceClient {

    Promise<MachineTokenDto> getMachineToken();

    Promise<UserDescriptor> getUserByToken(String token);
}
