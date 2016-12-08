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
package com.codenvy.resource.api.free;

import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import java.util.List;

/**
 * Provides default resources which should be are available for usage by account
 * when admin doesn't override limit by {@link FreeResourcesLimitService}.
 *
 * @author Sergii Leschenko
 */
public interface DefaultResourcesProvider {
    /**
     * Provides default resources are available for usage by account
     */
    List<ResourceImpl> getResources(String accountId) throws ServerException, NotFoundException;

    /**
     * Returns account type for which this class provides default resources.
     */
    String getAccountType();
}
