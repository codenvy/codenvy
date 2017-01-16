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
package com.codenvy.service.systemram;

import org.eclipse.che.api.core.ServerException;

/**
 * Provides system RAM information.
 * The specific of system RAM is depends on the implementation.
 *
 * @author Igor Vinokur
 */
public interface SystemRamInfoProvider {

    /**
     * Returns {@link SystemRamInfo} object, that describes system RAM values and properties.
     *
     * @throws ServerException if failed to retrieve system RAM values
     */
    SystemRamInfo getSystemRamInfo() throws ServerException;
}
