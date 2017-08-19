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

import static java.util.Arrays.asList;

import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.shared.model.Organization;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.api.free.DefaultResourcesProvider;
import com.codenvy.resource.api.type.RamResourceType;
import com.codenvy.resource.api.type.RuntimeResourceType;
import com.codenvy.resource.api.type.TimeoutResourceType;
import com.codenvy.resource.api.type.WorkspaceResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.Size;

/**
 * Provided free resources that are available for usage by organizational accounts by default.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class DefaultOrganizationResourcesProvider implements DefaultResourcesProvider {
  private final OrganizationManager organizationManager;
  private final long ramPerOrganization;
  private final int workspacesPerOrganization;
  private final int runtimesPerOrganization;
  private final long timeout;

  @Inject
  public DefaultOrganizationResourcesProvider(
      OrganizationManager organizationManager,
      @Named("limits.organization.workspaces.ram") String ramPerOrganization,
      @Named("limits.organization.workspaces.count") int workspacesPerOrganization,
      @Named("limits.organization.workspaces.run.count") int runtimesPerOrganization,
      @Named("limits.workspace.idle.timeout") long timeout) {
    this.timeout = TimeUnit.MILLISECONDS.toMinutes(timeout);
    this.organizationManager = organizationManager;
    this.ramPerOrganization =
        "-1".equals(ramPerOrganization) ? -1 : Size.parseSizeToMegabytes(ramPerOrganization);
    this.workspacesPerOrganization = workspacesPerOrganization;
    this.runtimesPerOrganization = runtimesPerOrganization;
  }

  @Override
  public String getAccountType() {
    return OrganizationImpl.ORGANIZATIONAL_ACCOUNT;
  }

  @Override
  public List<ResourceImpl> getResources(String accountId)
      throws ServerException, NotFoundException {
    final Organization organization = organizationManager.getById(accountId);
    // only root organizations should have own resources
    if (organization.getParent() == null) {
      return asList(
          new ResourceImpl(TimeoutResourceType.ID, timeout, TimeoutResourceType.UNIT),
          new ResourceImpl(RamResourceType.ID, ramPerOrganization, RamResourceType.UNIT),
          new ResourceImpl(
              WorkspaceResourceType.ID, workspacesPerOrganization, WorkspaceResourceType.UNIT),
          new ResourceImpl(
              RuntimeResourceType.ID, runtimesPerOrganization, RuntimeResourceType.UNIT));
    }

    return Collections.emptyList();
  }
}
