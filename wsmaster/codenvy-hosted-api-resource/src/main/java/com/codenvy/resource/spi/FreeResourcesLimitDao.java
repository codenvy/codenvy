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
package com.codenvy.resource.spi;

import com.codenvy.resource.spi.impl.FreeResourcesLimitImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;

/**
 * Defines data access object contract for {@link FreeResourcesLimitImpl}.
 *
 * @author Sergii Leschenko
 */
public interface FreeResourcesLimitDao {
    /**
     * Stores (creates new one or updates existed) free resource limit.
     *
     * @param resourcesLimit
     *         resources limit to store
     * @throws NullPointerException
     *         when {@code resourcesLimit} is null
     * @throws ServerException
     *         when any other error occurs
     */
    void store(FreeResourcesLimitImpl resourcesLimit) throws ServerException;

    /**
     * Returns free resources limit for account with specified id.
     *
     * @param accountId
     *         account id to fetch resources limit
     * @return free resources limit for account with specified id
     * @throws NullPointerException
     *         when {@code accountId} is null
     * @throws NotFoundException
     *         when free resources limit for specifies id was not found
     * @throws ServerException
     *         when any other error occurs
     */
    FreeResourcesLimitImpl get(String accountId) throws NotFoundException, ServerException;

    /**
     * Gets all free resources limits.
     *
     * @param maxItems
     *         the maximum number of limits to return
     * @param skipCount
     *         the number of limits to skip
     * @return list of limits POJO or empty list if no limits were found
     * @throws ServerException
     *         when any other error occurs
     */
    Page<FreeResourcesLimitImpl> getAll(int maxItems, int skipCount) throws ServerException;

    /**
     * Removes free resources limit for account with specified id.
     *
     * <p>Doesn't throw an exception when resources limit for specified {@code accountId} does not exist
     *
     * @param accountId
     *         account id to remove resources limit
     * @throws NullPointerException
     *         when {@code accountId} is null
     * @throws ServerException
     *         when any other error occurs
     */
    void remove(String accountId) throws ServerException;
}
