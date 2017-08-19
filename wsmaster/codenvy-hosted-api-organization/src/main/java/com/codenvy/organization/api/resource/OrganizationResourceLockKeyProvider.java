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
package com.codenvy.organization.api.resource;

import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.shared.model.Organization;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.api.ResourceLockKeyProvider;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

/**
 * Provides resources lock key for accounts with organizational type.
 *
 * <p>A lock key for any organization is an identifier of the root organization.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class OrganizationResourceLockKeyProvider implements ResourceLockKeyProvider {
  private final OrganizationManager organizationManager;

  @Inject
  public OrganizationResourceLockKeyProvider(OrganizationManager organizationManager) {
    this.organizationManager = organizationManager;
  }

  @Override
  public String getLockKey(String accountId) throws ServerException {
    String currentOrganizationId = accountId;
    try {
      Organization organization = organizationManager.getById(currentOrganizationId);
      while (organization.getParent() != null) {
        currentOrganizationId = organization.getParent();
        organization = organizationManager.getById(currentOrganizationId);
      }
      return organization.getId();
    } catch (NotFoundException e) {
      // should not happen
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  public String getAccountType() {
    return OrganizationImpl.ORGANIZATIONAL_ACCOUNT;
  }
}
