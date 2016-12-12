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

import com.codenvy.resource.model.FreeResourcesLimit;
import com.codenvy.resource.spi.FreeResourcesLimitDao;
import com.codenvy.resource.spi.impl.FreeResourcesLimitImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Objects.requireNonNull;

/**
 * Facade for free resources limit related operations.
 *
 * @author Sergii Leschenko
 */
//TODO Add checking resources availability before limit changing and removing
@Singleton
public class FreeResourcesLimitManager {
    private final FreeResourcesLimitDao freeResourcesLimitDao;

    @Inject
    public FreeResourcesLimitManager(FreeResourcesLimitDao freeResourcesLimitDao) {
        this.freeResourcesLimitDao = freeResourcesLimitDao;
    }

    /**
     * Stores (creates new one or updates existed) free resource limit.
     *
     * @param freeResourcesLimit
     *         resources limit to store
     * @return stored resources limit
     * @throws NullPointerException
     *         when {@code freeResourcesLimit} is null
     * @throws NotFoundException
     *         when resources limit contains resource with non supported type
     * @throws ServerException
     *         when any other error occurs
     */
    public FreeResourcesLimit store(FreeResourcesLimit freeResourcesLimit) throws NotFoundException,
                                                                                  ServerException {
        requireNonNull(freeResourcesLimit, "Required non-null free resources limit");
        final FreeResourcesLimitImpl toStore = new FreeResourcesLimitImpl(freeResourcesLimit);
        freeResourcesLimitDao.store(toStore);
        return toStore;
    }

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
    public FreeResourcesLimit get(String accountId) throws NotFoundException, ServerException {
        requireNonNull(accountId, "Required non-null account id");
        return freeResourcesLimitDao.get(accountId);
    }

    /**
     * Removes free resources limit for account with specified id.
     *
     * <p>After removing resources limit account will be able to use default resources
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
    public void remove(String accountId) throws ServerException {
        requireNonNull(accountId, "Required non-null account id");
        freeResourcesLimitDao.remove(accountId);
    }

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
    public Page<? extends FreeResourcesLimit> getAll(int maxItems, int skipCount) throws ServerException {
        return freeResourcesLimitDao.getAll(maxItems, skipCount);
    }
}
