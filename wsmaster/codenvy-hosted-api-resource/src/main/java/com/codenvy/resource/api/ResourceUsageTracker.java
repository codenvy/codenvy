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
package com.codenvy.resource.api;

import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import java.util.Optional;

/**
 * Tracks usage of resources of specified type.
 *
 * @author Sergii Leschenko
 */
public interface ResourceUsageTracker {
    /**
     * Returns used resource by given account.
     *
     * @param accountId
     *         account id to fetch used resource
     * @return used resource by given account
     * @throws NotFoundException
     *         when account with specified id was not found
     * @throws ServerException
     *         when some exception occurs on used resources fetching
     */
    Optional<ResourceImpl> getUsedResource(String accountId) throws NotFoundException, ServerException;
}
