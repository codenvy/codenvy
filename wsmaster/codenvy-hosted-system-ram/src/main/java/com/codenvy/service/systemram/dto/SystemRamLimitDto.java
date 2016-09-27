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
package com.codenvy.service.systemram.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Dto to describe system RAM limit status.
 *
 * @author Igor Vinokur
 */
@DTO
public interface SystemRamLimitDto {

    /**
     * Returns {@code true} if system RAM limit is exceeded, otherwise returns {@code false}.
     */
    boolean isSystemRamLimitExceeded();

    /**
     * Define if the system RAM limit is exceeded or not.
     */
    void setSystemRamLimitExceeded(boolean systemRamLimitExceeded);

    /**
     * Returns {@link SystemRamLimitDto} with defined system RAM limit state.
     */
    SystemRamLimitDto withSystemRamLimitExceeded(boolean systemRamLimitExceeded);
}
