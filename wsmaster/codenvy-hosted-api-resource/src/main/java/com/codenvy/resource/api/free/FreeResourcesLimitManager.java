/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.resource.api.free;

import static java.util.Objects.requireNonNull;

import com.codenvy.resource.model.FreeResourcesLimit;
import com.codenvy.resource.spi.FreeResourcesLimitDao;
import com.codenvy.resource.spi.impl.FreeResourcesLimitImpl;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;

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
   * @param freeResourcesLimit resources limit to store
   * @return stored resources limit
   * @throws NullPointerException when {@code freeResourcesLimit} is null
   * @throws NotFoundException when resources limit contains resource with non supported type
   * @throws ServerException when any other error occurs
   */
  public FreeResourcesLimit store(FreeResourcesLimit freeResourcesLimit)
      throws NotFoundException, ServerException {
    requireNonNull(freeResourcesLimit, "Required non-null free resources limit");
    final FreeResourcesLimitImpl toStore = new FreeResourcesLimitImpl(freeResourcesLimit);
    freeResourcesLimitDao.store(toStore);
    return toStore;
  }

  /**
   * Returns free resources limit for account with specified id.
   *
   * @param accountId account id to fetch resources limit
   * @return free resources limit for account with specified id
   * @throws NullPointerException when {@code accountId} is null
   * @throws NotFoundException when free resources limit for specifies id was not found
   * @throws ServerException when any other error occurs
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
   * <p>Doesn't throw an exception when resources limit for specified {@code accountId} does not
   * exist
   *
   * @param accountId account id to remove resources limit
   * @throws NullPointerException when {@code accountId} is null
   * @throws ServerException when any other error occurs
   */
  public void remove(String accountId) throws ServerException {
    requireNonNull(accountId, "Required non-null account id");
    freeResourcesLimitDao.remove(accountId);
  }

  /**
   * Gets all free resources limits.
   *
   * @param maxItems the maximum number of limits to return
   * @param skipCount the number of limits to skip
   * @return list of limits POJO or empty list if no limits were found
   * @throws ServerException when any other error occurs
   */
  public Page<? extends FreeResourcesLimit> getAll(int maxItems, int skipCount)
      throws ServerException {
    return freeResourcesLimitDao.getAll(maxItems, skipCount);
  }
}
